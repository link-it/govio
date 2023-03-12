import { AfterContentChecked, AfterViewInit, Component, HostListener, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { AbstractControl, UntypedFormControl, UntypedFormGroup } from '@angular/forms';
import { HttpParams } from '@angular/common/http';

import { MatFormFieldAppearance } from '@angular/material/form-field';

import { LangChangeEvent, TranslateService } from '@ngx-translate/core';

import { ConfigService } from 'projects/tools/src/lib/config.service';
import { Tools } from 'projects/tools/src/lib/tools.service';
import { EventsManagerService } from 'projects/tools/src/lib/eventsmanager.service';
import { PageloaderService } from 'projects/tools/src/lib/pageloader.service';
import { OpenAPIService } from 'projects/govio-app/src/services/openAPI.service';

import { SearchBarFormComponent } from 'projects/components/src/lib/ui/search-bar-form/search-bar-form.component';

import moment from 'moment';

@Component({
  selector: 'app-files',
  templateUrl: 'files.component.html',
  styleUrls: ['files.component.scss']
})
export class FilesComponent implements OnInit, AfterViewInit, AfterContentChecked, OnDestroy {
  static readonly Name = 'FilesComponent';
  readonly model: string = 'files';

  @ViewChild('searchBarForm') searchBarForm!: SearchBarFormComponent;

  Tools = Tools;

  config: any;
  filesConfig: any;

  files: any[] = [];
  page: any = {};
  _links: any = {};

  _isEdit: boolean = false;
  _editCurrent: any = null;

  _hasFilter: boolean = true;
  _formGroup: UntypedFormGroup = new UntypedFormGroup({});
  _filterData: any[] = [];

  _preventMultiCall: boolean = false;

  _spin: boolean = true;
  desktop: boolean = false;

  _useRoute : boolean = true;

  _materialAppearance: MatFormFieldAppearance = 'fill';

  _message: string = 'APP.MESSAGE.NoResults';
  _messageHelp: string = 'APP.MESSAGE.NoResultsHelp';
  _messageUnimplemented: string = 'APP.MESSAGE.Unimplemented';
  _messageNoResponseUnimplemented: string = 'APP.MESSAGE.NoResponseUnimplemented';

  _error: boolean = false;

  showHistory: boolean = true;
  showSearch: boolean = true;
  showSorting: boolean = true;

  sortField: string = 'date';
  sortDirection: string = 'asc';
  sortFields: any[] = [];

  searchFields: any[] = [
    { field: 'creationDateFrom', label: 'APP.LABEL.Date', type: 'date', condition: 'gt', format: 'DD/MM/YYYY' },
    { field: 'creationDateTo', label: 'APP.LABEL.Date', type: 'date', condition: 'lt', format: 'DD/MM/YYYY' },
    { field: 'fileName', label: 'APP.LABEL.FileName', type: 'string', condition: 'like' },
    { field: 'status', label: 'APP.LABEL.Status', type: 'enum', condition: 'equal', enumValues: { 'NUOVO': 'APP.STATUS.NUOVO', 'ELABORAZIONE': 'APP.STATUS.ELABORAZIONE', 'COMPLETATO': 'APP.STATUS.COMPLETATO', 'SCARTATO': 'APP.STATUS.SCARTATO' } },
    { field: 'type', label: 'APP.LABEL.Type', type: 'enum', condition: 'equal', enumValues: { 'CBI': 'CBI' } }
  ];

  breadcrumbs: any[] = [
    { label: 'APP.TITLE.Files', url: '', type: 'title', icon: 'topic' }
  ];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private translate: TranslateService,
    private configService: ConfigService,
    public tools: Tools,
    private eventsManagerService: EventsManagerService,
    private pageloaderService: PageloaderService,
    public apiService: OpenAPIService
  ) {
    this.config = this.configService.getConfiguration();
    this._materialAppearance = this.config.materialAppearance;

    this._initSearchForm();
  }

  @HostListener('window:resize') _onResize() {
    this.desktop = (window.innerWidth >= 992);
  }

  ngOnInit() {
    this.translate.onLangChange.subscribe((event: LangChangeEvent) => {
      // language
    });

    this.pageloaderService.resetLoader();
    this.pageloaderService.isLoading.subscribe({
      next: (x) => { this._spin = x; },
      error: (e: any) => { console.log('loader error', e); }
    });

    this.configService.getConfig('files').subscribe(
      (config: any) => {
        this.filesConfig = config;
        this._translateConfig();
      }
    );
  }

  ngOnDestroy() {}

  ngAfterViewInit() {
    if (!(this.searchBarForm && this.searchBarForm._isPinned())) {
      setTimeout(() => {
        this._loadFiles();
      }, 100);
    }
  }

  ngAfterContentChecked(): void {
    this.desktop = (window.innerWidth >= 992);
  }

  _translateConfig() {
    if (this.filesConfig && this.filesConfig.options) {
      Object.keys(this.filesConfig.options).forEach((key: string) => {
        if (this.filesConfig.options[key].label) {
          this.filesConfig.options[key].label = this.translate.instant(this.filesConfig.options[key].label);
        }
        if (this.filesConfig.options[key].values) {
          Object.keys(this.filesConfig.options[key].values).forEach((key2: string) => {
            this.filesConfig.options[key].values[key2].label = this.translate.instant(this.filesConfig.options[key].values[key2].label);
          });
        }
      });
    }
  }

  _setErrorMessages(error: boolean) {
    this._error = error;
    if (this._error) {
      this._message = 'APP.MESSAGE.ERROR.Default';
      this._messageHelp = 'APP.MESSAGE.ERROR.DefaultHelp';
    } else {
      this._message = 'APP.MESSAGE.NoResults';
      this._messageHelp = 'APP.MESSAGE.NoResultsHelp';
    }
  }

  _initSearchForm() {
    this._formGroup = new UntypedFormGroup({
      creationDateFrom: new UntypedFormControl(''),
      creationDateTo: new UntypedFormControl(''),
      fileName: new UntypedFormControl(''),
      status: new UntypedFormControl(''),
      type: new UntypedFormControl(''),
    });
  }

  _loadFiles(query: any = null, url: string = '') {
    this._setErrorMessages(false);

    if (!url) { this.files = []; }
    
    let aux: any;
    if (query)  aux = { params: this._queryToHttpParams(query) };

    this.apiService.getList(this.model, aux, url).subscribe({
      next: (response: any) => {
        this.page = response.page;
        this._links = response._links;

        if (response.items) {
          const _itemRow = this.filesConfig.itemRow;
          const _options = this.filesConfig.options;
          const _list: any = response.items.map((file: any) => {
            const metadataText = Tools.simpleItemFormatter(_itemRow.metadata.text, file, _options || null);
            const metadataLabel = Tools.simpleItemFormatter(_itemRow.metadata.label, file, _options || null);
            const element = {
              id: file.id,
              primaryText: Tools.simpleItemFormatter(_itemRow.primaryText, file, _options || null),
              secondaryText: Tools.simpleItemFormatter(_itemRow.secondaryText, file, _options || null, ' '),
              metadata: `${metadataText}<span class="me-2">&nbsp;</span>${metadataLabel}`,
              secondaryMetadata: Tools.simpleItemFormatter(_itemRow.secondaryMetadata, file, _options || null, ' '),
              editMode: false,
              source: { ...file }
            };
            return element;
          });
          this.files = (url) ? [...this.files, ..._list] : [..._list];
          this._preventMultiCall = false;
        }
        Tools.ScrollTo(0);
      },
      error: (error: any) => {
        this._setErrorMessages(true);
        this._preventMultiCall = false;
        // Tools.OnError(error);
      }
    });
  }

  _queryToHttpParams(query: any) : HttpParams {
    let httpParams = new HttpParams();

    Object.keys(query).forEach(key => {
      if (query[key]) {
        let _dateTime = '';
        switch (key)
        {
          case 'data_inizio':
          case 'data_fine':
            _dateTime = moment(query[key]).format('YYYY-MM-DD');
            httpParams = httpParams.set(key, _dateTime);
            break;
          default:
            httpParams = httpParams.set(key, query[key]);
        }
      }
    });
    
    return httpParams; 
  }

  __loadMoreData() {
    if (this._links && this._links.next && !this._preventMultiCall) {
      this._preventMultiCall = true;
      this._loadFiles(null, this._links.next.href);
    }
  }

  _onNew() {
    if (this._useRoute) {
      this.router.navigate([this.model, 'new']);
    } else {
      this._isEdit = true;
    }
  }

  _onEdit(event: any, param: any) {
    if (this._useRoute) {
      if (this.searchBarForm) {
        this.searchBarForm._pinLastSearch();
      }
      this.router.navigate(['files', param.id]);
    } else {
      this._isEdit = true;
      this._editCurrent = param;
    }
  }

  _onCloseEdit() {
    this._isEdit = false;
  }

  _dummyAction(event: any, param: any) {
    console.log(event, param);
  }

  _onSubmit(form: any) {
    if (this.searchBarForm) {
      this.searchBarForm._onSearch();
    }
  }

  _onSearch(values: any) {
    this._filterData = values;
    this._loadFiles(this._filterData);
  }

  _resetForm() {
    this._filterData = [];
    this._loadFiles(this._filterData);
  }

  _onSort(event: any) {
    console.log(event);
  }

  _timestampToMoment(value: number) {
    return value ? new Date(value) : null;
  }

  onBreadcrumb(event: any) {
    this.router.navigate([event.url]);
  }

  _resetScroll() {
    Tools.ScrollElement('container-scroller', 0);
  }
}

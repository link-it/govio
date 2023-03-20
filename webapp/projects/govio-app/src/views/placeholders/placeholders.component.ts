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
  selector: 'app-placeholders',
  templateUrl: 'placeholders.component.html',
  styleUrls: ['placeholders.component.scss']
})
export class PlaceholdersComponent implements OnInit, AfterViewInit, AfterContentChecked, OnDestroy {
  static readonly Name = 'PlaceholdersComponent';
  readonly model: string = 'placeholders';

  @ViewChild('searchBarForm') searchBarForm!: SearchBarFormComponent;

  Tools = Tools;

  config: any;
  placeholdersConfig: any;

  placeholders: any[] = [];
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

  _placeholder: string = 'APP.PLACEHOLDER.NoResults';
  _placeholderHelp: string = 'APP.PLACEHOLDER.NoResultsHelp';
  _placeholderUnimplemented: string = 'APP.PLACEHOLDER.Unimplemented';
  _placeholderNoResponseUnimplemented: string = 'APP.PLACEHOLDER.NoResponseUnimplemented';

  _error: boolean = false;

  showHistory: boolean = true;
  showSearch: boolean = true;
  showSorting: boolean = true;

  sortField: string = 'date';
  sortDirection: string = 'asc';
  sortFields: any[] = [];

  searchFields: any[] = [];

  breadcrumbs: any[] = [
    { label: 'APP.TITLE.Configurations', url: '', type: 'title', iconBs: 'gear' },
    { label: 'APP.TITLE.Placeholders', url: '', type: 'link'}
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

    this.configService.getConfig('placeholders').subscribe(
      (config: any) => {
        this.placeholdersConfig = config;
        this._translateConfig();
      }
    );
  }

  ngOnDestroy() {}

  ngAfterViewInit() {
    if (!(this.searchBarForm && this.searchBarForm._isPinned())) {
      setTimeout(() => {
        this._loadPlaceholders();
      }, 100);
    }
  }

  ngAfterContentChecked(): void {
    this.desktop = (window.innerWidth >= 992);
  }

  _translateConfig() {
    if (this.placeholdersConfig && this.placeholdersConfig.options) {
      Object.keys(this.placeholdersConfig.options).forEach((key: string) => {
        if (this.placeholdersConfig.options[key].label) {
          this.placeholdersConfig.options[key].label = this.translate.instant(this.placeholdersConfig.options[key].label);
        }
        if (this.placeholdersConfig.options[key].values) {
          Object.keys(this.placeholdersConfig.options[key].values).forEach((key2: string) => {
            this.placeholdersConfig.options[key].values[key2].label = this.translate.instant(this.placeholdersConfig.options[key].values[key2].label);
          });
        }
      });
    }
  }

  _setErrorPlaceholders(error: boolean) {
    this._error = error;
    if (this._error) {
      this._placeholder = 'APP.PLACEHOLDER.ERROR.Default';
      this._placeholderHelp = 'APP.PLACEHOLDER.ERROR.DefaultHelp';
    } else {
      this._placeholder = 'APP.PLACEHOLDER.NoResults';
      this._placeholderHelp = 'APP.PLACEHOLDER.NoResultsHelp';
    }
  }

  _initSearchForm() {
    this._formGroup = new UntypedFormGroup({
      'q': new UntypedFormControl(''),
    });
  }

  _loadPlaceholders(query: any = null, url: string = '') {
    this._setErrorPlaceholders(false);

    let aux: any;
    if (!url) {
      this.placeholders = [];
      if (query) { aux = { params: this._queryToHttpParams(query) } };
    }

    this.apiService.getList(this.model, aux, url).subscribe({
      next: (response: any) => {
        this.page = response.page;
        this._links = response._links;

        if (response.items) {
          const _list: any = response.items.map((placeholder: any) => {
            const metadataText = Tools.simpleItemFormatter(this.placeholdersConfig.itemRow.metadata.text, placeholder, this.placeholdersConfig.options || null);
            const metadataLabel = Tools.simpleItemFormatter(this.placeholdersConfig.itemRow.metadata.label, placeholder, this.placeholdersConfig.options || null);
            const element = {
              id: placeholder.id,
              primaryText: Tools.simpleItemFormatter(this.placeholdersConfig.itemRow.primaryText, placeholder, this.placeholdersConfig.options || null, ' '),
              secondaryText: Tools.simpleItemFormatter(this.placeholdersConfig.itemRow.secondaryText, placeholder, this.placeholdersConfig.options || null, ' '),
              metadata: `${metadataText}<span class="me-2">&nbsp;</span>${metadataLabel}`,
              secondaryMetadata: Tools.simpleItemFormatter(this.placeholdersConfig.itemRow.secondaryMetadata, placeholder, this.placeholdersConfig.options || null, ' '),
              editMode: false,
              source: { ...placeholder }
            };
            return element;
          });
          this.placeholders = (url) ? [...this.placeholders, ..._list] : [..._list];
          this._preventMultiCall = false;
        }
        Tools.ScrollTo(0);
      },
      error: (error: any) => {
        this._setErrorPlaceholders(true);
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
      this._loadPlaceholders(null, this._links.next.href);
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
      this.router.navigate(['placeholders', param.id]);
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
    this._loadPlaceholders(this._filterData);
  }

  _resetForm() {
    this._filterData = [];
    this._loadPlaceholders(this._filterData);
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

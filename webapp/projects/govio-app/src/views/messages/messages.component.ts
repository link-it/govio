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
  selector: 'app-messages',
  templateUrl: 'messages.component.html',
  styleUrls: ['messages.component.scss']
})
export class MessagesComponent implements OnInit, AfterViewInit, AfterContentChecked, OnDestroy {
  static readonly Name = 'MessagesComponent';
  readonly model: string = 'messages';

  @ViewChild('searchBarForm') searchBarForm!: SearchBarFormComponent;

  Tools = Tools;

  config: any;
  messagesConfig: any;

  messages: any[] = [];
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
    { field: 'scheduled_expedition_date_from', label: 'APP.LABEL.ScheduledExpeditionDate', type: 'date', condition: 'gt', format: 'DD/MM/YYYY' },
    { field: 'scheduled_expedition_date_to', label: 'APP.LABEL.ScheduledExpeditionDate', type: 'date', condition: 'lt', format: 'DD/MM/YYYY' },
    { field: 'organization.legal_name', label: 'APP.LABEL.LegalName', type: 'string', condition: 'like' },
    { field: 'service.service_name', label: 'APP.LABEL.ServiceName', type: 'text', condition: 'like' }
  ];

  breadcrumbs: any[] = [
    { label: 'APP.TITLE.Messages', url: '', type: 'title', iconBs: 'send' }
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

    this.configService.getConfig('messages').subscribe(
      (config: any) => {
        this.messagesConfig = config;
        this._translateConfig();
      }
    );
  }

  ngOnDestroy() {}

  ngAfterViewInit() {
    if (!(this.searchBarForm && this.searchBarForm._isPinned())) {
      setTimeout(() => {
        this._loadMessages();
      }, 100);
    }
  }

  ngAfterContentChecked(): void {
    this.desktop = (window.innerWidth >= 992);
  }

  _translateConfig() {
    if (this.messagesConfig && this.messagesConfig.options) {
      Object.keys(this.messagesConfig.options).forEach((key: string) => {
        if (this.messagesConfig.options[key].label) {
          this.messagesConfig.options[key].label = this.translate.instant(this.messagesConfig.options[key].label);
        }
        if (this.messagesConfig.options[key].values) {
          Object.keys(this.messagesConfig.options[key].values).forEach((key2: string) => {
            this.messagesConfig.options[key].values[key2].label = this.translate.instant(this.messagesConfig.options[key].values[key2].label);
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
      scheduled_expedition_date_from: new UntypedFormControl(''),
      scheduled_expedition_date_to: new UntypedFormControl(''),
      'organization.legal_name': new UntypedFormControl(''),
      'service.service_name': new UntypedFormControl(''),
    });
  }

  _loadMessages(query: any = null, url: string = '') {
    this._setErrorMessages(false);

    if (!url) { this.messages = []; }

    let aux: any;
    query = { ...query, embed: ['sender']};
    aux = { params: this._queryToHttpParams(query) };

    this.apiService.getList(this.model, aux, url).subscribe({
      next: (response: any) => {
        this.page = response.page;
        this._links = response._links;

        if (response.items) {
          const _itemRow = this.messagesConfig.itemRow;
          const _options = this.messagesConfig.options;
          const _list: any = response.items.map((message: any) => {
            const metadataText = Tools.simpleItemFormatter(_itemRow.metadata.text, message, _options || null);
            const metadataLabel = Tools.simpleItemFormatter(_itemRow.metadata.label, message, _options || null);
            message.sender = message._embedded.sender;
            const element = {
              id: message.id,
              primaryText: Tools.simpleItemFormatter(_itemRow.primaryText, message, _options || null, ' '),
              secondaryText: Tools.simpleItemFormatter(_itemRow.secondaryText, message, _options || null, ' '),
              metadata: `${metadataText}<span class="me-2">&nbsp;</span>${metadataLabel}`,
              secondaryMetadata: Tools.simpleItemFormatter(_itemRow.secondaryMetadata, message, _options || null, ' '),
              editMode: false,
              source: { ...message }
            };
            return element;
          });
          this.messages = (url) ? [...this.messages, ..._list] : [..._list];
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
      this._loadMessages(null, this._links.next.href);
    }
  }

  _onEdit(event: any, param: any) {
    if (this._useRoute) {
      if (this.searchBarForm) {
        this.searchBarForm._pinLastSearch();
      }
      this.router.navigate(['messages', param.id]);
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
    this._loadMessages(this._filterData);
  }

  _resetForm() {
    this._filterData = [];
    this._loadMessages(this._filterData);
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

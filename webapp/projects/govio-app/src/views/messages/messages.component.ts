import { AfterContentChecked, AfterViewInit, Component, HostListener, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { AbstractControl, UntypedFormControl, UntypedFormGroup } from '@angular/forms';

import { MatFormFieldAppearance } from '@angular/material/form-field';

import { LangChangeEvent, TranslateService } from '@ngx-translate/core';

import { ConfigService } from 'projects/tools/src/lib/config.service';
import { Tools } from 'projects/tools/src/lib/tools.service';
import { EventsManagerService } from 'projects/tools/src/lib/eventsmanager.service';
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

  _spin: boolean = false;
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
    { field: 'messageName', label: 'APP.LABEL.MessageName', type: 'string', condition: 'like' },
    { field: 'status', label: 'APP.LABEL.Status', type: 'enum', condition: 'equal', enumValues: { 'NUOVO': 'APP.STATUS.NUOVO', 'ELABORAZIONE': 'APP.STATUS.ELABORAZIONE', 'COMPLETATO': 'APP.STATUS.COMPLETATO', 'SCARTATO': 'APP.STATUS.SCARTATO' } },
    { field: 'type', label: 'APP.LABEL.Type', type: 'enum', condition: 'equal', enumValues: { 'CBI': 'CBI' } }
  ];

  breadcrumbs: any[] = [
    { label: 'APP.TITLE.Messages', url: '', type: 'title', icon: 'send' }
  ];

  _unimplemented: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private translate: TranslateService,
    private configService: ConfigService,
    public tools: Tools,
    private eventsManagerService: EventsManagerService,
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
      setTimeout(() => {
        Tools.WaitForResponse(false);
      }, this.config.AppConfig.DELAY || 0);
    });

    Tools.WaitForResponse(true, false, false);
    this.configService.getConfig('messages').subscribe(
      (config: any) => {
        this.messagesConfig = config;
        this._translateConfig();
        Tools.WaitForResponse(false);
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
    this._spin = this.tools.getSpinner() && !this.tools.isSpinnerGlobal();
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
      creationDateFrom: new UntypedFormControl(''),
      creationDateTo: new UntypedFormControl(''),
      messageName: new UntypedFormControl(''),
      status: new UntypedFormControl(''),
      type: new UntypedFormControl(''),
    });
  }

  _loadMessages(query: any = null, url: string = '') {
    this._setErrorMessages(false);
    if (!url) { this.messages = []; }
    this.apiService.getList(this.model, query, url).subscribe({
      next: (response: any) => {
        if (response === null) {
          this._unimplemented = true;
        } else {

          this.page = response.page;
          this._links = response._links;

          if (response.items) {
            const _list: any = response.items.map((message: any) => {
              const metadataText = Tools.simpleItemFormatter(this.messagesConfig.simpleItem.metadata.text, message, this.messagesConfig.simpleItem.options || null);
              const metadataLabel = Tools.simpleItemFormatter(this.messagesConfig.simpleItem.metadata.label, message, this.messagesConfig.simpleItem.options || null);
              const element = {
                id: message.id,
                primaryText: Tools.simpleItemFormatter(this.messagesConfig.simpleItem.primaryText, message, this.messagesConfig.simpleItem.options || null),
                secondaryText: Tools.simpleItemFormatter(this.messagesConfig.simpleItem.secondaryText, message, this.messagesConfig.simpleItem.options || null),
                metadata: `${metadataText}<span class="me-2">&nbsp;</span>${metadataLabel}`,
                secondaryMetadata: Tools.simpleItemFormatter(this.messagesConfig.simpleItem.secondaryMetadata, message, this.messagesConfig.simpleItem.options || null),
                editMode: false,
                source: { ...message }
              };
              return element;
            });
            this.messages = (url) ? [...this.messages, ..._list] : [..._list];
            this._preventMultiCall = false;
          }
          Tools.ScrollTo(0);
        }
      },
      error: (error: any) => {
        this._setErrorMessages(true);
        this._preventMultiCall = false;
        // Tools.OnError(error);
      }
    });
  }

  __loadMoreData() {
    if (this._links.next && !this._preventMultiCall) {
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

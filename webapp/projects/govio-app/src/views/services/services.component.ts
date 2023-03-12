import { AfterContentChecked, Component, HostListener, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { UntypedFormControl, UntypedFormGroup } from '@angular/forms';

import { MatFormFieldAppearance } from '@angular/material/form-field';

import { LangChangeEvent, TranslateService } from '@ngx-translate/core';

import { ConfigService } from 'projects/tools/src/lib/config.service';
import { Tools } from 'projects/tools/src/lib/tools.service';
import { EventsManagerService } from 'projects/tools/src/lib/eventsmanager.service';
import { OpenAPIService } from 'projects/govio-app/src/services/openAPI.service';
import { PageloaderService } from 'projects/tools/src/lib/pageloader.service';
import { SearchBarFormComponent } from 'projects/components/src/lib/ui/search-bar-form/search-bar-form.component';

import * as jsonpatch from 'fast-json-patch';

@Component({
  selector: 'app-services',
  templateUrl: 'services.component.html',
  styleUrls: ['services.component.scss']
})
export class ServicesComponent implements OnInit, AfterContentChecked, OnDestroy {
  static readonly Name = 'ServicesComponent';
  readonly model: string = 'services';

  @ViewChild('searchBarForm') searchBarForm!: SearchBarFormComponent;

  config: any;
  servicesConfig: any;

  services: any[] = [];
  page: any = {};
  _links: any = {};

  _isEdit: boolean = false;

  _hasFilter: boolean = false;
  _formGroup: UntypedFormGroup = new UntypedFormGroup({});
  _filterData: any[] = [];

  _preventMultiCall: boolean = false;

  _spin: boolean = false;
  desktop: boolean = false;

  _materialAppearance: MatFormFieldAppearance = 'fill';

  _message: string = 'APP.MESSAGE.NoResults';
  _messageHelp: string = 'APP.MESSAGE.NoResultsHelp';
  _messageUnimplemented: string = 'APP.MESSAGE.Unimplemented';

  _error: boolean = false;

  showHistory: boolean = true;
  showSearch: boolean = true;
  showSorting: boolean = true;

  sortField: string = 'service_name';
  sortDirection: string = 'asc';
  sortFields: any[] = [];

  searchFields: any[] = [];

  _useRoute: boolean = true;

  breadcrumbs: any[] = [
    { label: 'APP.TITLE.Services', url: '', type: 'title', icon: 'apps' }
  ];

  _unimplemented: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private translate: TranslateService,
    private configService: ConfigService,
    public tools: Tools,
    private eventsManagerService: EventsManagerService,
    public apiService: OpenAPIService,
    public pageloaderService: PageloaderService
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
      // Changed
    });

    this.pageloaderService.resetLoader();
    this.pageloaderService.isLoading.subscribe({
      next: (x) => { this._spin = x; },
      error: (e: any) => { console.log('loader error', e); }
    });

    this.configService.getConfig(this.model).subscribe(
      (config: any) => {
        this.servicesConfig = config;
        this._translateConfig();
        this._loadServices();
      }
    );
  }

  ngOnDestroy() {
    // this.eventsManagerService.off(EventType.NAVBAR_ACTION);
  }

  ngAfterContentChecked(): void {
    this.desktop = (window.innerWidth >= 992);
  }

  _translateConfig() {
    if (this.servicesConfig && this.servicesConfig.options) {
      Object.keys(this.servicesConfig.options).forEach((key: string) => {
        if (this.servicesConfig.options[key].label) {
          this.servicesConfig.options[key].label = this.translate.instant(this.servicesConfig.options[key].label);
        }
        if (this.servicesConfig.options[key].values) {
          Object.keys(this.servicesConfig.options[key].values).forEach((key2: string) => {
            this.servicesConfig.options[key].values[key2].label = this.translate.instant(this.servicesConfig.options[key].values[key2].label);
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
    this._formGroup = new UntypedFormGroup({});
  }

  _loadServices(query: any = null, url: string = '') {
    this._setErrorMessages(false);
    if (!url) { this.services = []; }
    this.apiService.getList(this.model).subscribe({
      next: (response: any) => {
        if (response === null) {
          this._unimplemented = true;
        } else {

          this.page = response.page;
          this._links = response._links;

          if (response.items) {
            const _list: any = response.items.map((service: any) => {
              const metadataText = Tools.simpleItemFormatter(this.servicesConfig.simpleItem.metadata.text, service, this.servicesConfig.simpleItem.options || null);
              const metadataLabel = Tools.simpleItemFormatter(this.servicesConfig.simpleItem.metadata.label, service, this.servicesConfig.simpleItem.options || null);
              const element = {
                id: service.id,
                primaryText: Tools.simpleItemFormatter(this.servicesConfig.simpleItem.primaryText, service, this.servicesConfig.simpleItem.options || null),
                secondaryText: Tools.simpleItemFormatter(this.servicesConfig.simpleItem.secondaryText, service, this.servicesConfig.simpleItem.options || null),
                metadata: `${metadataText}<span class="me-2">&nbsp;</span>${metadataLabel}`,
                secondaryMetadata: Tools.simpleItemFormatter(this.servicesConfig.simpleItem.secondaryMetadata, service, this.servicesConfig.simpleItem.options || null),
                editMode: false,
                source: { ...service }
              };
              return element;
            });
            this.services = (url) ? [...this.services, ..._list] : [..._list];
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
    if (this._links && this._links.next && !this._preventMultiCall) {
      this._preventMultiCall = true;
      this._loadServices(null, this._links.next.href);
    }
  }

  _onEdit(event: any, param: any) {
    if (this._useRoute) {
      this.router.navigate([this.model, param.id]);
    } else {
      this._isEdit = true;
    }
  }

  _onNew() {
    if (this._useRoute) {
      this.router.navigate([this.model, 'new']);
    } else {
      this._isEdit = true;
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
    this._loadServices(this._filterData);
  }

  _resetForm() {
    this._filterData = [];
    this._loadServices(this._filterData);
  }

  _onSort(event: any) {
    console.log(event);
  }

  onBreadcrumb(event: any) {
    this.router.navigate([event.url]);
  }

  _resetScroll() {
    Tools.ScrollElement('container-scroller', 0);
  }
}

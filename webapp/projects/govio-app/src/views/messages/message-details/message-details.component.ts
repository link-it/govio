import { AfterContentChecked, Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { AbstractControl, UntypedFormControl, UntypedFormGroup, Validators } from '@angular/forms';
import { HttpParams } from '@angular/common/http';

import { LangChangeEvent, TranslateService } from '@ngx-translate/core';

import { ConfigService } from 'projects/tools/src/lib/config.service';
import { Tools } from 'projects/tools/src/lib/tools.service';
import { EventsManagerService } from 'projects/tools/src/lib/eventsmanager.service';
import { OpenAPIService } from 'projects/govio-app/src/services/openAPI.service';
import { PageloaderService } from 'projects/tools/src/lib/pageloader.service';
import { FieldClass } from 'projects/link-lab/src/lib/it/link/classes/definitions';

import { Message } from './message';

@Component({
  selector: 'app-message-details',
  templateUrl: 'message-details.component.html',
  styleUrls: ['message-details.component.scss']
})
export class MessageDetailsComponent implements OnInit, OnChanges, AfterContentChecked, OnDestroy {
  static readonly Name = 'MessageDetailsComponent';
  readonly model: string = 'messages';

  @Input() id: number | null = null;
  @Input() message: any = null;
  @Input() config: any = null;

  @Output() close: EventEmitter<any> = new EventEmitter<any>();
  @Output() save: EventEmitter<any> = new EventEmitter<any>();

  _title: string = '';

  appConfig: any;

  _informazioni: FieldClass[] = [];

  _isDetails = true;

  _message: Message = new Message({});

  _sender: any = null;
  _serviceInstance: any = null;
  _organization: any = null;
  _service: any = null;
  _template: any = null;

  _spin: boolean = true;
  desktop: boolean = false;

  _useRoute: boolean = true;

  breadcrumbs: any[] = [];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private translate: TranslateService,
    private configService: ConfigService,
    private tools: Tools,
    private eventsManagerService: EventsManagerService,
    private apiService: OpenAPIService,
    private pageloaderService: PageloaderService
  ) {
    this.appConfig = this.configService.getConfiguration();
  }

  ngOnInit() {
    // this.pageloaderService.resetLoader();
    // this.pageloaderService.isLoading.subscribe({
    //   next: (x) => { this._spin = x; },
    //   error: (e: any) => { console.log('loader error', e); }
    // });

    this.route.params.subscribe(params => {
      if (params['id'] && params['id'] !== 'new') {
        this.id = params['id'];
        this._initBreadcrumb();
        this._isDetails = true;
        this.configService.getConfig(this.model).subscribe(
          (config: any) => {
            this.config = config;
            this._translateConfig();
            this._loadAll();
          }
        );
      }
    });
  }

  ngOnDestroy() {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.id) {
      this.id = changes.id.currentValue;
      this._loadAll();
    }
    if (changes.message) {
      const message = changes.message.currentValue;
      this.message = message.source;
      this.id = this.message.id;
    }
  }

  ngAfterContentChecked(): void {
    this.desktop = (window.innerWidth >= 992);
  }

  _loadAll() {
    this._loadMessage();
  }

  _loadMessage() {
    if (this.id) {
      this._spin = true;
      this.message = null;
      let aux: any = { params: this._queryToHttpParams({ embed: ['service-instance'] }) };
      this.apiService.getDetails(this.model, this.id, '', aux).subscribe({
        next: (response: any) => {
          this.message = response; // new Message({ ...response });
          this._message = new Message({ ...response });
          // this.__initInformazioni();

          this._loadSender(this.message.sender_id);
        },
        error: (error: any) => {
          this._spin = false;
          Tools.OnError(error);
        }
      });
    }
  }

  _queryToHttpParams(query: any) : HttpParams {
    let httpParams = new HttpParams();

    Object.keys(query).forEach(key => {
      if (query[key]) {
        switch (key)
        {
          default:
            httpParams = httpParams.set(key, query[key]);
        }
      }
    });
    
    return httpParams; 
  }

  _loadSender(id: number) {
    this._spin = true;
    this._sender = null;
    this.apiService.getDetails('users', id).subscribe({
      next: (response: any) => {
        this._sender = response;
        this._loadServiceInstance(this.message.service_instance_id);
      },
      error: (error: any) => {
        this._spin = false;
        Tools.OnError(error);
      }
    });
  }

  _loadServiceInstance(id: number) {
    this._spin = true;
    this._serviceInstance = null;
    let aux: any = { params: this._queryToHttpParams({ embed: ['organization','service','template'] }) };
    this.apiService.getDetails('service-instances', id, '', aux).subscribe({
      next: (response: any) => {
        this._serviceInstance = response;
        this._organization = this._serviceInstance._embedded.organization;
        this._service = this._serviceInstance._embedded.service;
        this._template = this._serviceInstance._embedded.template;

        this.message.user = this._sender;
        this.message.organization = this._organization;
        this.message.service = this._service;
        this.message.template = this._template;

        this._spin = false;
      },
      error: (error: any) => {
        this._spin = false;
        Tools.OnError(error);
      }
    });
  }

  __initInformazioni() {
    if (this.message) {
      this._informazioni = Tools.generateFields(this.config.details, this.message, true, this.config.options).map((field: FieldClass) => {
        field.label = this.translate.instant(field.label);
        return field;
      });
    }
  }

  _translateConfig() {
    if (this.config && this.config.options) {
      Object.keys(this.config.options).forEach((key: string) => {
        if (this.config.options[key].label) {
          this.config.options[key].label = this.translate.instant(this.config.options[key].label);
        }
        if (this.config.options[key].values) {
          Object.keys(this.config.options[key].values).forEach((key2: string) => {
            this.config.options[key].values[key2].label = this.translate.instant(this.config.options[key].values[key2].label);
          });
        }
      });
    }
  }

  _initBreadcrumb() {
    const _title = this.id ? `#${this.id}` : this.translate.instant('APP.TITLE.New');
    this.breadcrumbs = [
      { label: '', url: '', type: 'title', iconBs: 'send' },
      { label: 'APP.TITLE.Messages', url: '/messages', type: 'link' },
      { label: `${_title}`, url: '', type: 'title' }
    ];
  }

  _dummyAction(event: any, param: any) {
    console.log(event, param);
  }

  onBreadcrumb(event: any) {
    if (this._useRoute) {
      this.router.navigate([event.url]);
    }
  }
}

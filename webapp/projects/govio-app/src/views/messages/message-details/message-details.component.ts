import { AfterContentChecked, Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { AbstractControl, UntypedFormControl, UntypedFormGroup, Validators } from '@angular/forms';

import { LangChangeEvent, TranslateService } from '@ngx-translate/core';
import { BsModalService, BsModalRef } from 'ngx-bootstrap/modal';

import { ConfigService } from 'projects/tools/src/lib/config.service';
import { Tools } from 'projects/tools/src/lib/tools.service';
import { EventsManagerService } from 'projects/tools/src/lib/eventsmanager.service';
import { OpenAPIService } from 'projects/govio-app/src/services/openAPI.service';
import { PageloaderService } from 'projects/tools/src/lib/pageloader.service';
import { FieldClass } from 'projects/link-lab/src/lib/it/link/classes/definitions';

import { YesnoDialogBsComponent } from 'projects/components/src/lib/dialogs/yesno-dialog-bs/yesno-dialog-bs.component';

import { Message } from './message';

import * as jsonpatch from 'fast-json-patch';

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

  hasTab: boolean = true;
  tabs: any[] = [
    { label: 'Details', icon: 'details', link: 'details', enabled: true }
  ];
  _currentTab: string = 'details';

  _informazioni: FieldClass[] = [];

  _isDetails = true;

  _isEdit = false;
  _closeEdit = true;
  _isNew = false;
  _formGroup: UntypedFormGroup = new UntypedFormGroup({});
  _message: Message = new Message({});

  messageProviders: any = null;

  _spin: boolean = true;
  desktop: boolean = false;

  _useRoute: boolean = true;

  breadcrumbs: any[] = [];

  _error: boolean = false;
  _errorMsg: string = '';

  _modalConfirmRef!: BsModalRef;

  _imagePlaceHolder: string = './assets/images/logo-placeholder.png';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private translate: TranslateService,
    private modalService: BsModalService,
    private configService: ConfigService,
    public tools: Tools,
    public eventsManagerService: EventsManagerService,
    public apiService: OpenAPIService,
    public pageloaderService: PageloaderService
  ) {
    this.appConfig = this.configService.getConfiguration();
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
      } else {
        this._isNew = true;
        this._isEdit = true;

        this._initBreadcrumb();
        // this._loadAnagrafiche();

        if (this._isEdit) {
          this._initForm({ ...this._message });
        } else {
          this._loadAll();
        }
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
    // this._loadMessageProviders();
  }

  _hasControlError(name: string) {
    return (this.f[name].errors && this.f[name].touched);
  }

  get f(): { [key: string]: AbstractControl } {
    return this._formGroup.controls;
  }

  _initForm(data: any = null) {
    if (data) {
      let _group: any = {};
      Object.keys(data).forEach((key) => {
        let value = '';
        switch (key) {
          case 'legal_name':
            value = data[key] ? data[key] : null;
            _group[key] = new UntypedFormControl(value, [Validators.required]);
            break;
          case 'tax_code':
            const pattern = /^[0-9]{11}$/;
            value = data[key] ? data[key] : null;
            _group[key] = new UntypedFormControl(value, [
              Validators.required,
              Validators.pattern(pattern)
            ]);
            break;
          case 'office_email_address':
            value = data[key] ? data[key] : null;
            _group[key] = new UntypedFormControl(value, [
              Validators.required,
              Validators.email
            ]);
            break;
          default:
            value = data[key] ? data[key] : null;
            _group[key] = new UntypedFormControl(value, []);
            break;
        }
      });
      this._formGroup = new UntypedFormGroup(_group);
    }
  }

  _onImageLoaded(event: any, field: string) {
    const _base64 = event ? window.btoa(event) : null; // btoa - atob
    this._formGroup.get(field)?.setValue(_base64);
  }

  // _decodeImage(data: string): string {
  //   return data ? window.atob(data) : this._imagePlaceHolder;
  // }
  _decodeImage = (data: string): string => {
    return data ? window.atob(data) : this._imagePlaceHolder;
  };

  __onSave(body: any) {
    this._error = false;
    this.apiService.saveElement(this.model, body).subscribe(
      (response: any) => {
        this.message = new Message({ ...response });
        this._message = new Message({ ...response });
        this.id = this.message.id;
        this._initBreadcrumb();
        this._isEdit = false;
        this._isNew = false;
        this.save.emit({ id: this.id, payment: response, update: false });
      },
      (error: any) => {
        this._error = true;
        this._errorMsg = Tools.GetErrorMsg(error);
      }
    );
  }

  __onUpdate(id: number, body: any) {
    this._error = false;
    const _bodyPatch: any[] = jsonpatch.compare(this.message, body);
    if (_bodyPatch) {
      console.log('__onUpdate', _bodyPatch);
      this.apiService.updateElement(this.model, id, _bodyPatch).subscribe(
        (response: any) => {
          this._isEdit = !this._closeEdit;
          this.message = new Message({ ...response });
          this._message = new Message({ ...response });
          this.id = this.message.id;
          this.save.emit({ id: this.id, payment: response, update: true });
        },
        (error: any) => {
          this._error = true;
          this._errorMsg = Tools.GetErrorMsg(error);
        }
      );
    } else {
      console.log('No difference');
    }
  }

  _onSubmit(form: any, close: boolean = true) {
    if (this._isEdit && this._formGroup.valid) {
      this._closeEdit = close;
      if (this._isNew) {
        this.__onSave(form);
      } else {
        this.__onUpdate(this.message.id, form);
      }
    }
  }

  _deleteMessage() {
    const initialState = {
      title: this.translate.instant('APP.TITLE.Attention'),
      messages: [
        this.translate.instant('APP.MESSAGE.AreYouSure')
      ],
      cancelText: this.translate.instant('APP.BUTTON.Cancel'),
      confirmText: this.translate.instant('APP.BUTTON.Confirm'),
      confirmColor: 'danger'
    };

    this._modalConfirmRef = this.modalService.show(YesnoDialogBsComponent, {
      ignoreBackdropClick: true,
      initialState: initialState
    });
    this._modalConfirmRef.content.onClose.subscribe(
      (response: any) => {
        if (response) {
          this.apiService.deleteElement(this.model, this.message.id).subscribe(
            (response) => {
              this.save.emit({ id: this.id, message: response, update: false });
            },
            (error) => {
              this._error = true;
              this._errorMsg = Tools.GetErrorMsg(error);
            }
          );
        }
      }
    );
  }

  _loadMessage() {
    if (this.id) {
      this.message = null;
      this.apiService.getDetails(this.model, this.id).subscribe({
        next: (response: any) => {
          this.message = new Message({ ...response });
          this._message = new Message({ ...response });
          this._title = this.message.creditorReferenceId;
          if (this.config.detailsTitle) {
            this._title = Tools.simpleItemFormatter(this.config.detailsTitle, this.message);
          }
          // this.__initInformazioni();
        },
        error: (error: any) => {
          Tools.OnError(error);
        }
      });
    }
  }

  __initInformazioni() {
    if (this.message) {
      this._informazioni = Tools.generateFields(this.config.details, this.message).map((field: FieldClass) => {
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
      { label: '', url: '', type: 'title', icon: 'corporate_fare' },
      { label: 'APP.TITLE.Messages', url: '/messages', type: 'link' },
      { label: `${_title}`, url: '', type: 'title' }
    ];
  }

  _clickTab(tab: string) {
    this._currentTab = tab;
  }

  _dummyAction(event: any, param: any) {
    console.log(event, param);
  }

  _editMessage() {
    this._initForm({ ...this._message });
    this._isEdit = true;
    this._error = false;
  }

  _onClose() {
    this.close.emit({ id: this.id, message: this._message });
  }

  _onSave() {
    this.save.emit({ id: this.id, message: this._message });
  }

  _onCancelEdit() {
    this._isEdit = false;
    this._error = false;
    this._errorMsg = '';
    if (this._isNew) {
      if (this._useRoute) {
        this.router.navigate([this.model]);
      } else {
        this.close.emit({ id: this.id, message: null });
      }
    } else {
      this._message = new Message({ ...this.message });
    }
  }

  onBreadcrumb(event: any) {
    if (this._useRoute) {
      this.router.navigate([event.url]);
    } else {
      this._onClose();
    }
  }
}

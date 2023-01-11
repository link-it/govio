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

import { File } from './file';

declare const saveAs: any;

@Component({
  selector: 'app-file-details',
  templateUrl: 'file-details.component.html',
  styleUrls: ['file-details.component.scss']
})
export class FileDetailsComponent implements OnInit, OnChanges, AfterContentChecked, OnDestroy {
  static readonly Name = 'FileDetailsComponent';
  readonly model: string = 'files';

  @Input() id: number | null = null;
  @Input() file: any = null;
  @Input() config: any = null;

  @Output() close: EventEmitter<any> = new EventEmitter<any>();
  @Output() save: EventEmitter<any> = new EventEmitter<any>();

  appConfig: any;

  hasTab: boolean = true;
  tabs: any[] = [
    { label: 'Details', icon: 'details', link: 'details', enabled: true }
  ];
  _currentTab: string = 'details';

  _informazioni: FieldClass[] = [];

  _isDetails = true;

  _editable: boolean = false;
  _deleteable: boolean = false;
  _isEdit = false;
  _closeEdit = true;
  _isNew = false;
  _formGroup: UntypedFormGroup = new UntypedFormGroup({});
  _file: File = new File({});

  fileProviders: any = null;

  _spin: boolean = true;
  desktop: boolean = false;

  _useRoute: boolean = true;

  breadcrumbs: any[] = [];

  _error: boolean = false;
  _errorMsg: string = '';

  _modalConfirmRef!: BsModalRef;

  _filePlaceHolder: string = './assets/images/logo-placeholder.png';
  _organizationLogoPlaceholder: string = './assets/images/organization-placeholder.png';
  _serviceLogoPlaceholder: string = './assets/images/service-placeholder.png';

  _organizations: any[] = [];
  _services: any[] = [];
  _selectedFile: any = null;

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

        this.configService.getConfig(this.model).subscribe(
          (config: any) => {
            this.config = config;
            this._translateConfig();
            if (this._isEdit) {
              this._loadRegistry();
              this._initForm({ ...this._file });
            } else {
              this._loadAll();
            }
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
    if (changes.file) {
      const file = changes.file.currentValue;
      this.file = file.source;
      this.id = this.file.id;
    }
  }

  ngAfterContentChecked(): void {
    this.desktop = (window.innerWidth >= 992);
  }

  _loadAll() {
    this._loadRegistry();
    this._loadFile();
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
          case 'organization_id':
          case 'service_id':
          case 'file':
            value = data[key] ? data[key] : null;
            _group[key] = new UntypedFormControl(value, [Validators.required]);
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

  _onFileLoaded(event: any, field: string) {
    this._selectedFile = event.target.files[0];
  }

  __onSave(body: any) {
    this._error = false;
    const formData = new FormData();
    formData.append('file', this._selectedFile, this._selectedFile.name);
    const url: string = `files?organization_id=${body.organization_id}&service_id=${body.service_id}`;
    this.apiService.upload(url, formData)
      .subscribe({
        next: (response: any) => {
          this.id = response.id;
          this.file = response; // new File({ ...response });
          this._file = response; // new File({ ...response });
          this._isNew = false;

          this._initBreadcrumb();
          this.__initInformazioni();
          this._onCancelEdit();
        },
        error: (error: any) => {
          this._error = true;
          this._errorMsg = Tools.GetErrorMsg(error);
        }
      });
  }

  __onUpdate(id: number, body: any) {
    // this._error = false;
    // const _bodyPatch: any[] = jsonpatch.compare(this.file, body);
    // if (_bodyPatch) {
    //   this.apiService.updateElement(this.model, id, _bodyPatch).subscribe(
    //     (response: any) => {
    //       this._isEdit = !this._closeEdit;
    //       this.file = new File({ ...response });
    //       this._file = new File({ ...response });
    //       this.id = this.file.id;
    //       this.save.emit({ id: this.id, payment: response, update: true });
    //     },
    //     (error: any) => {
    //       this._error = true;
    //       this._errorMsg = Tools.GetErrorMsg(error);
    //     }
    //   );
    // } else {
    //   console.log('No difference');
    // }
  }

  _onSubmit(form: any, close: boolean = true) {
    if (this._isEdit && this._formGroup.valid) {
      this._closeEdit = close;
      if (this._isNew) {
        this.__onSave(form);
      } else {
        this.__onUpdate(this.file.id, form);
      }
    }
  }

  _deleteFile() {
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
          this.apiService.deleteElement(this.model, this.file.id).subscribe(
            (response) => {
              this.save.emit({ id: this.id, file: response, update: false });
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

  _loadRegistry() {
    this.apiService.getList('organizations').subscribe({
      next: (response: any) => {
        this._organizations = response.items;
      },
      error: (error: any) => {
        Tools.OnError(error);
      }
    });

    this.apiService.getList('services').subscribe({
      next: (response: any) => {
        this._services = response.items;
      },
      error: (error: any) => {
        Tools.OnError(error);
      }
    });

    // this.apiService.getList('service-instances').subscribe({
    //   next: (response: any) => {
    //     this._services = response.items;
    //   },
    //   error: (error: any) => {
    //     Tools.OnError(error);
    //   }
    // });
  }

  _downloadAction(event: any) {
    this._downloadContent(event.item);
  }

  _downloadContent(item: any) {
    if (this.id) {
      Tools.WaitForResponse(true, false, false);
      this.apiService.download(this.model, this.id, 'content').subscribe({
        next: (response: any) => {
          Tools.WaitForResponse(false);
          let name: string = this.file.filename ?? 'file_' + this.id + '.txt';
          saveAs(response, name);
        },
        error: (error: any) => {
          Tools.WaitForResponse(false);
          Tools.OnError(error);
        }
      });
    }
  }

  _loadFile() {
    if (this.id) {
      this.file = null;
      this.apiService.getDetails(this.model, this.id).subscribe({
        next: (response: any) => {
          this.file = response; // new File({ ...response });
          this._file = response; // new File({ ...response });

          this.__initInformazioni();
        },
        error: (error: any) => {
          Tools.OnError(error);
        }
      });
    }
  }

  __initInformazioni() {
    if (this.file) {
      this._informazioni = Tools.generateFields(this.config.details, this.file, true, this.config.options).map((field: FieldClass) => {
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
      { label: 'APP.TITLE.Files', url: '/files', type: 'link' },
      { label: `${_title}`, url: '', type: 'title' }
    ];
  }

  _clickTab(tab: string) {
    this._currentTab = tab;
  }

  _dummyAction(event: any, param: any) {
    console.log(event, param);
  }

  _editFile() {
    this._initForm({ ...this._file });
    this._isEdit = true;
    this._error = false;
  }

  _onClose() {
    this.close.emit({ id: this.id, file: this._file });
  }

  _onSave() {
    this.save.emit({ id: this.id, file: this._file });
  }

  _onCancelEdit() {
    this._isEdit = false;
    this._error = false;
    this._errorMsg = '';
    if (this._isNew) {
      if (this._useRoute) {
        this.router.navigate([this.model]);
      } else {
        this.close.emit({ id: this.id, file: null });
      }
    } else {
      this._file = new File({ ...this.file });
    }
  }

  onBreadcrumb(event: any) {
    if (this._useRoute) {
      this.router.navigate([event.url]);
    } else {
      this._onClose();
    }
  }

  _orgLogo = (item: any): string => {
    let logoUrl = this._organizationLogoPlaceholder;
    if (item._links && item._links.logo_small) {
      logoUrl = item._links.logo_small.href;
    }
    return logoUrl;
  };

  _orgLogoBackground = (item: any): string => {
    let logoUrl = this._organizationLogoPlaceholder;
    if (item._links && item._links.logo_small && false) {
      // logoUrl = 'http://172.16.1.121:8083/govio/api/v1/organizations/16/logo_miniature';
      logoUrl = item._links.logo_small.href;
    }
    return `url(${logoUrl})`;
  };

  _serviceLogoBackground = (item: any): string => {
    const logoUrl = item.logo || this._serviceLogoPlaceholder;
    return `url(${logoUrl})`;
  };
}

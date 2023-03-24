import { AfterContentChecked, Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { AbstractControl, UntypedFormControl, UntypedFormGroup, Validators } from '@angular/forms';
import { HttpParams } from '@angular/common/http';

import { LangChangeEvent, TranslateService } from '@ngx-translate/core';
import { BsModalService, BsModalRef } from 'ngx-bootstrap/modal';

import { ConfigService } from 'projects/tools/src/lib/config.service';
import { Tools } from 'projects/tools/src/lib/tools.service';
import { EventsManagerService } from 'projects/tools/src/lib/eventsmanager.service';
import { OpenAPIService } from 'projects/govio-app/src/services/openAPI.service';
import { PageloaderService } from 'projects/tools/src/lib/pageloader.service';
import { FieldClass } from 'projects/link-lab/src/lib/it/link/classes/definitions';

import { YesnoDialogBsComponent } from 'projects/components/src/lib/dialogs/yesno-dialog-bs/yesno-dialog-bs.component';

import { concat, Observable, of, Subject, throwError } from 'rxjs';
import { catchError, debounceTime, distinctUntilChanged, filter, map, mergeMap, startWith, switchMap, tap } from 'rxjs/operators';

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
  _exportSpin: boolean = false;

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

  _serviceInstance: any = null;
  _organization: any = null;
  _service: any = null;
  _template: any = null;

  minLengthTerm = 1;

  services$!: Observable<any[]>;
  servicesSelected$!: any;
  servicesInput$ = new Subject<string>();
  servicesLoading: boolean = false;

  _data: any[] = [];

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
    this.route.params.subscribe(params => {
      if (params['id'] && params['id'] !== 'new') {
        this.id = params['id'];
        this._initBreadcrumb();
        this._isDetails = true;
        this.configService.getConfig(this.model).subscribe(
          (config: any) => {
            this.config = config;
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
            if (this._isEdit) {
              this._initForm({ ...this._file });
              this._initServicesSelect([]);
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
          case 'service_instance':
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
    const url: string = `files?service_instance=${body.service_instance}`;
    this.apiService.upload(url, formData)
      .subscribe({
        next: (response: any) => {
          this.id = response.id;
          this.file = response; // new File({ ...response });
          this._file = response; // new File({ ...response });
          this._isNew = false;

          this._initBreadcrumb();
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
      this._spin = true;
      this.file = null;
      this.apiService.getDetails(this.model, this.id).subscribe({
        next: (response: any) => {
          this.file = response; // new File({ ...response });
          this._file = response; // new File({ ...response });

          this._spin = false;
          this._loadServiceInstance(this.file.service_instance_id);
        },
        error: (error: any) => {
          this._spin = false;
          Tools.OnError(error);
        }
      });
    }
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

        this.file.organization = this._organization;
        this.file.service = this._service;
        this.file.template = this._template;

        this._spin = false;
      },
      error: (error: any) => {
        this._spin = false;
        Tools.OnError(error);
      }
    });
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

  _initBreadcrumb() {
    const _title = this.id ? `#${this.id}` : this.translate.instant('APP.TITLE.New');
    this.breadcrumbs = [
      { label: '', url: '', type: 'title', icon: 'account_balance' },
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
    if (item._links && item._links.logo) {
      logoUrl = item._links.logo.href;
    }
    return logoUrl;
  };

  _orgLogoBackground = (item: any): string => {
    let logoUrl = this._organizationLogoPlaceholder;
    if (item._links && item._links.logo && false) {
      logoUrl = item._links.logo.href;
    }
    return `url(${logoUrl})`;
  };

  _serviceLogoBackground = (item: any): string => {
    const logoUrl = item.logo || this._serviceLogoPlaceholder;
    return `url(${logoUrl})`;
  };

  trackByFn(item: any) {
    return item.id;
  }

  _initServicesSelect(defaultValue: any[] = []) {
    this.services$ = concat(
      of(defaultValue),
      this.servicesInput$.pipe(
        // filter(res => {
        //   return res !== null && res.length >= this.minLengthTerm
        // }),
        startWith(''),
        debounceTime(300),
        distinctUntilChanged(),
        tap(() => this.servicesLoading = true),
        switchMap((term: any) => {
          return this.getData('service-instances', term).pipe(
            catchError(() => of([])), // empty list on error
            tap(() => this.servicesLoading = false)
          )
        })
      )
    );
  }

  getData(model: string, term: string | null = null): Observable<any> {
    const _options: any = { params: { q: term, limit: 100, embed: ['service','organization','template'] } };

    return this.apiService.getList(model, _options)
      .pipe(map(resp => {
        if (resp.Error) {
          throwError(resp.Error);
        } else {
          const _items = resp.items.map((item: any) => {
            item.organization = item._embedded.organization;
            item.organization_name = item._embedded.organization.legal_name;
            item.service = item._embedded.service;
            item.service_name = item._embedded.service.service_name;
            item.template = item._embedded.template;
            item.template_name = item._embedded.template.description;
            item.label = `${item.service_name} | ${item.template_name}`;
            // item.disabled = true;
            return item;
          });
          return _items;
        }
      })
      );
  }

  onChangeService(event: any) {
    this.servicesSelected$ = event;
  }

  onExport() {
    let _data: any[] = [];
    this._exportSpin = true;
    let aux: any = { params: this._queryToHttpParams({ file_messages_status: 'error', limit: 100 }) };
    this.apiService.getDetails(this.model, this.id, 'file-messages', aux)
      .pipe(
        switchMap(response => {
          _data = _data.concat(response.items);
          if(response._links && response._links.next) {
            return this._getData(response._links.next.href, aux, _data);
          } else {
            return of(_data);
          }
        })
      ).subscribe({
        next: (response: any) => {
          this._data = response;
          Tools.DownloadCSVFile(this._data, 'FileMessagesError');
          this._exportSpin = false;
        },
        error: (error: any) => {
          this._exportSpin = false;
          Tools.OnError(error);
        }
      });
  }

  _getData(url: string, aux: any, fullData:any[]): Observable<any> {
    fullData = fullData || [];
    return this.apiService.getList(this.model, aux, url)
      .pipe(
        switchMap((data:any)=>{
          fullData = fullData.concat(data.items);
          return !data.next? of(fullData):
            this._getData(data.next, aux, fullData)
        })
      );
  }
}

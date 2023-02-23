import { Component, HostListener, Input, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { AbstractControl, UntypedFormControl, UntypedFormGroup } from '@angular/forms';
import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';

import { TranslateService } from '@ngx-translate/core';
import { BsModalService, BsModalRef } from 'ngx-bootstrap/modal';

import { YesnoDialogBsComponent } from 'projects/components/src/lib/dialogs/yesno-dialog-bs/yesno-dialog-bs.component';

import { ConfigService } from 'projects/tools/src/lib/config.service';
import { EventsManagerService } from 'projects/tools/src/lib/eventsmanager.service';
import { OpenAPIService } from 'projects/govio-app/src/services/openAPI.service';

@Component({
  selector: 'app-template-placeholders',
  templateUrl: 'template-placeholders.component.html',
  styleUrls: ['template-placeholders.component.scss']
})
export class TemplatePlaceholdersComponent implements OnInit, OnDestroy {

  @Input() id: number | null = null;

  config: any;

  templatePlaceholders: any[] = [];
  _origTemplatePlaceholders: any[] = [];
  page: any = {};
  _links: any = {};

  _isEdit: boolean = false;
  _isNew: boolean = false;
  _editPlaceholders: boolean = false;
  _modifiedPlaceholders: boolean = false;

  _formGroup: UntypedFormGroup = new UntypedFormGroup({});

  _spin: boolean = false;

  _message: string = 'APP.MESSAGE.NoResults';
  _messageHelp: string = 'APP.MESSAGE.NoResultsHelp';

  _error: boolean = false;

  placeholdersConfig: any = null;

  _modalConfirmRef!: BsModalRef;

  constructor(
    private translate: TranslateService,
    private modalService: BsModalService,
    private configService: ConfigService,
    private eventsManagerService: EventsManagerService,
    public apiService: OpenAPIService
  ) {
  }

  ngOnInit() {
    this._loadTemplatePlaceholders();

    this.configService.getConfig('placeholders').subscribe(
      (config: any) => {
        this.placeholdersConfig = config;
        this._translateConfig();
      }
    );

  }

  ngOnDestroy() {
  }

  refresh() {
    this._loadTemplatePlaceholders();
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

  _loadTemplatePlaceholders(query: any = null, url: string = '') {
    this._setErrorMessages(false);
    if (this.id) {
      this._spin = true;
      // if (!url) { this.templatePlaceholders = []; }
      this.apiService.getList(`templates/${this.id}/placeholders?embed=placeholder`).subscribe({
        next: (response: any) => {
          this.templatePlaceholders = response.items;
          this._origTemplatePlaceholders = response.items ? JSON.parse(JSON.stringify(response.items)) : null;
          this._spin = false;
        },
        error: (error: any) => {
          this._setErrorMessages(true);
          this._spin = false;
        }
      });
    }
  }

  _onNew() {
    this._isNew = !this._isNew;
  }

  _onSave(event: any) {
    this._isEdit = false;
    this._isNew = false;
    this.refresh();
  }

  _onDelete(event: any) {
    const _pId = event.item._embedded.placeholder.id;

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
          this._spin = true;
          this.apiService.deleteElementRelated('templates', this.id, `placeholders/${_pId}`).subscribe({
            next: (response: any) => {
              this.refresh();
              this._spin = false;
            },
            error: (e: any) => {
              this._setErrorMessages(true);
              this._spin = false;
            }
          });
        }
      }
    );
  }

  _onClose(event: any) {
    this._isEdit = false;
  }

  _onEditPlaceholders(event: any) {
    this._editPlaceholders = !this._editPlaceholders;
    if (!this._editPlaceholders && this._modifiedPlaceholders) {
      this._modifiedPlaceholders = false;
      this.templatePlaceholders = JSON.parse(JSON.stringify(this._origTemplatePlaceholders));
    }
  }

  _onSavePlaceholders(event: any) {
    this._editPlaceholders = false;
    this.refresh();
  }

  drop(event: any) {
    const _prevItem = this.templatePlaceholders[event.previousIndex];
    const _currentItem = this.templatePlaceholders[event.currentIndex];
    moveItemInArray(this.templatePlaceholders, event.previousIndex, event.currentIndex);
    console.log('drop', this.templatePlaceholders);
    if (event.previousIndex !== event.currentIndex) {
      this._modifiedPlaceholders = true;
    }
  }
}

import { Component, EventEmitter, Input, OnDestroy, OnInit, Output, ViewChild } from '@angular/core';
import { AbstractControl, FormControl, UntypedFormControl, UntypedFormGroup, Validators } from '@angular/forms';

import { TranslateService } from '@ngx-translate/core';

import { Tools } from 'projects/tools/src/lib/tools.service';
import { EventsManagerService } from 'projects/tools/src/lib/eventsmanager.service';
import { OpenAPIService } from 'projects/govio-app/src/services/openAPI.service';
import { CustomValidators } from 'projects/tools/src/lib/custom-forms-validators/custom-forms.module';

import { PlaceholderItem } from './placeholder-item';

import * as jsonpatch from 'fast-json-patch';

@Component({
  selector: 'app-placeholder-item',
  templateUrl: 'placeholder-item.component.html',
  styleUrls: ['placeholder-item.component.scss']
})
export class PlaceholderItemComponent implements OnInit, OnDestroy {

  @Input() templateId: number | null = null;
  @Input() data: any = null;
  @Input() config: any = null;
  @Input() editable: boolean = true;
  
  @Output() close: EventEmitter<any> = new EventEmitter();
  @Output() save: EventEmitter<any> = new EventEmitter();
  @Output() delete: EventEmitter<any> = new EventEmitter();
  
  _isEdit: boolean = false;
  _isNew: boolean = false;

  _dataSrc: any = null;

  _formData: PlaceholderItem = new PlaceholderItem({});
  _formGroup: UntypedFormGroup = new UntypedFormGroup({});

  _spin: boolean = false;

  _message: string = 'APP.MESSAGE.NoResults';
  _messageHelp: string = 'APP.MESSAGE.NoResultsHelp';

  _error: boolean = false;
  _errorMsg: string = '';

  constructor(
    private translate: TranslateService,
    public tools: Tools,
    private eventsManagerService: EventsManagerService,
    public apiService: OpenAPIService
  ) {
  }

  ngOnInit() {
    this._isNew = !this.data;
    this._isEdit = this._isNew;
    this._formData = {
      templateId: this.templateId,
      placeholderId: this.data?._embedded ? this.data._embedded.placeholder.id : null,
      position: this.data ? this.data.position : null,
      mandatory: this.data ? this.data.mandatory : false
    };
    this._initForm(this._formData);

    this._dataSrc = { source: { ...this.data?._embedded?.placeholder } };
  }

  ngOnDestroy() {
  }

  _initForm(data: any = null) {
    if (data) {
      let _group: any = {};
      Object.keys(data).forEach((key) => {
        let value = '';
        switch (key) {
          case 'templateId':
          case 'placeholderId':
          case 'position':
            value = data[key] ? data[key] : 0;
            _group[key] = new UntypedFormControl(value, [
              Validators.required,
              CustomValidators.gt(0)
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

  _onEdit(event: any) {
    this._isEdit = true;
  }

  __onSave(form: any) {
    const _body: any = {
      mandatory: form.mandatory || false,
      position: Number(form.position)
    };
    this.apiService.saveElement(`templates/${this.templateId}/placeholders?placeholder_id=${form.placeholderId}`, _body).subscribe(
      (response: any) => {
        this._isEdit = false;
        this.data = response;
        this.save.emit({ item: this.data });
      },
      (error: any) => {
        this._error = true;
        this._errorMsg = Tools.GetErrorMsg(error);
      }
    );
  }

  __removeEmpty(obj: any) {
    const $this = this;
    return Object.keys(obj)
      .filter(function (k) {
        return obj[k] != null;
      })
      .reduce(function (acc: any, k: string) {
        acc[k] = typeof obj[k] === "object" ? $this.__removeEmpty(obj[k]) : obj[k];
        return acc;
      }, {});
  }

  __onUpdate(form: any) {
    this._error = false;
    const _data = this.__removeEmpty(this.data);
    const _body = this.__removeEmpty(form);
    const _bodyPatch: any[] = jsonpatch.compare(_data, _body);
    if (_bodyPatch) {
      this.apiService.updateElementRelated('templates', this.templateId, `placeholders?placeholder_id=${form.placeholderId}`,_bodyPatch).subscribe(
        (response: any) => {
          this._isEdit = false;
          this.data = response;
          this.save.emit({ item: this.data, update: true });
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

  _onSubmit(form: any) {
    if (this._isEdit && this._formGroup.valid) {
      if (this._isNew) {
        this.__onSave(form);
      } else {
        this.__onUpdate(form);
      }
    }
  }

  _onDelete(event: any) {
    this.delete.emit({ item: this.data });
  }

  _onCloseEdit(event: any) {
    this._isEdit = false;
    this.close.emit({ item: this.data });
  }
}

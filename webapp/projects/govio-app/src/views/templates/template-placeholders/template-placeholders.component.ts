import { Component, HostListener, Input, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { AbstractControl, UntypedFormControl, UntypedFormGroup } from '@angular/forms';
import {CdkDragDrop, moveItemInArray} from '@angular/cdk/drag-drop';

import { TranslateService } from '@ngx-translate/core';

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
  page: any = {};
  _links: any = {};

  _isEdit: boolean = false;
  _editCurrent: any = null;

  _formGroup: UntypedFormGroup = new UntypedFormGroup({});

  _spin: boolean = false;

  _useRoute : boolean = false;

  _message: string = 'APP.MESSAGE.NoResults';
  _messageHelp: string = 'APP.MESSAGE.NoResultsHelp';

  _error: boolean = false;

  constructor(
    private translate: TranslateService,
    private eventsManagerService: EventsManagerService,
    public apiService: OpenAPIService
  ) {
  }

  ngOnInit() {
    this._loadTemplatePlaceholders();
  }

  ngOnDestroy() {
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
      if (!url) { this.templatePlaceholders = []; }
      this.apiService.getList(`templates/${this.id}/placeholders?embed=placeholder`).subscribe({
        next: (response: any) => {
          // this.page = response.page;
          // this._links = response._links;
          this.templatePlaceholders = response.items;
        },
        error: (error: any) => {
          this._setErrorMessages(true);
        }
      });
    }
  }

  _onEdit(event: any, param: any) {
    this._editCurrent = param;
    this._isEdit = true;
  }

  _onCloseEdit(event: any) {
    this._isEdit = false;
  }

  drop(event: any) {
    const _prevItem = this.templatePlaceholders[event.previousIndex];
    const _currentItem = this.templatePlaceholders[event.currentIndex];
    moveItemInArray(this.templatePlaceholders, event.previousIndex, event.currentIndex);
    console.log('drop', this.templatePlaceholders);
  }
}

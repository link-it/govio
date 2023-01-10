import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ItemTypeComponent } from './item-type.component';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatRippleModule } from '@angular/material/core';

@NgModule({
  declarations: [
    ItemTypeComponent
  ],
  imports: [
    CommonModule,
    MatCheckboxModule,
    MatRippleModule
  ],
  exports: [
    ItemTypeComponent
  ]
})
export class ItemTypeModule { }

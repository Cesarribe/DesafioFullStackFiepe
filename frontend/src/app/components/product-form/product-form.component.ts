import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { NgxMaskDirective, provideNgxMask } from 'ngx-mask'; 

@Component({
  selector: 'app-product-form',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    SidebarComponent,
    NgxMaskDirective 
  ],
  providers: [provideNgxMask()], 
  templateUrl: './product-form.component.html',
  styleUrls: ['./product-form.component.scss']
})
export class ProductFormComponent {
  product = {
    name: '',
    category: '',
    description: '',
    price: null,
    stock: null
  };

  salvarProduto(): void {
    console.log('Produto a ser salvo:', this.product);
  }

  cancelar(): void {
    console.log('Cadastro cancelado');
  }
}

import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-product-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
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


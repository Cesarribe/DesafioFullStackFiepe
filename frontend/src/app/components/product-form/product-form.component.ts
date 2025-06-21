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
    price: null,
    imageUrl: ''
  };

  salvarProduto(): void {
    console.log('Enviando produto:', this.product);
    // Aqui vamos fazer o POST /api/v1/products em breve
  }
}

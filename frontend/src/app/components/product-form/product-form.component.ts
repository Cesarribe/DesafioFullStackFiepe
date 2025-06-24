import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { NgxMaskDirective, provideNgxMask } from 'ngx-mask';
import { Product } from '../../models/product.model';
import { ProductService } from '../../services/product.service';

@Component({
  selector: 'app-product-form',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    HttpClientModule,
    SidebarComponent,
    NgxMaskDirective
  ],
  providers: [provideNgxMask()],
  templateUrl: './product-form.component.html',
  styleUrls: ['./product-form.component.scss']
})
export class ProductFormComponent implements OnInit {
  product: Product = {
    name: '',
    category: '',
    description: '',
    price: 0,
    stock: 0,
    created_at: ''
  };

  produtos: Product[] = [];

  constructor(private productService: ProductService) {}

  ngOnInit(): void {
    this.listarProdutos();
  }

  listarProdutos(): void {
    this.productService.listarProdutos().subscribe({
      next: (produtos) => (this.produtos = produtos),
      error: (err) => console.error('Erro ao carregar lista:', err)
    });
  }

  salvarProduto(): void {
    const nome = this.product.name?.trim().replace(/\s+/g, ' ');
    const regexNome = /^[\p{L}0-9\s\-_.,]+$/u;

    if (!nome || nome.length < 3 || nome.length > 100 || !regexNome.test(nome)) {
      alert('Nome inválido. Verifique os requisitos.');
      return;
    }

    const precoString = (this.product.price || '').toString().replace(/\./g, '').replace(',', '.');
    const preco = parseFloat(precoString);

    if (isNaN(preco) || preco < 0.01 || preco > 1000000) {
      alert('Preço fora do intervalo permitido.');
      return;
    }

    const estoque = this.product.stock;

    if (isNaN(estoque) || estoque < 0 || estoque > 999999) {
      alert('Estoque inválido.');
      return;
    }

    const descricao = (this.product.description || '').trim();
    if (descricao.length > 300) {
      alert('Descrição excede 300 caracteres.');
      return;
    }

    const novoProduto: Product = {
      ...this.product,
      name: nome,
      category: this.product.category?.trim(),
      description: descricao,
      price: preco,
      stock: estoque,
      created_at: new Date().toISOString()
    };

    if (this.product.id) {
      // Atualiza
      this.productService.atualizarProduto({ ...novoProduto, id: this.product.id }).subscribe({
        next: () => {
          alert('Produto atualizado!');
          this.resetarFormulario();
          this.listarProdutos();
        },
        error: (err) => console.error('Erro ao atualizar:', err)
      });
    } else {
      // Cria novo
      this.productService.salvarProduto(novoProduto).subscribe({
        next: () => {
          alert('Produto cadastrado!');
          this.resetarFormulario();
          this.listarProdutos();
        },
        error: (err) => {
          alert('Erro ao salvar. Tente novamente.');
          console.error(err);
        }
      });
    }
  }

  cancelar(): void {
    this.resetarFormulario();
    console.log('Cadastro cancelado');
  }

  venderProduto(produto: Product): void {
    if (produto.stock <= 0) return;
    const atualizado = { ...produto, stock: produto.stock - 1 };
    this.productService.atualizarProduto(atualizado).subscribe({
      next: () => this.listarProdutos(),
      error: (err) => console.error('Erro ao vender produto:', err)
    });
  }

  editarProduto(produto: Product): void {
    this.product = { ...produto };
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  excluirProduto(produto: Product): void {
    if (!confirm(`Tem certeza que deseja excluir "${produto.name}"?`)) return;
    this.productService.excluirProduto(produto.id!).subscribe({
      next: () => this.listarProdutos(),
      error: (err) => console.error('Erro ao excluir produto:', err)
    });
  }

  private resetarFormulario(): void {
    this.product = {
      name: '',
      category: '',
      description: '',
      price: 0,
      stock: 0,
      created_at: ''
    };
  }
}

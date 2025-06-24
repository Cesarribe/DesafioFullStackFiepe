import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SidebarComponent } from '../sidebar/sidebar.component'; // ajuste o caminho se necessário
import { Product } from '../../models/product.model';
import { ProductService } from '../../services/product.service';
import { Router } from '@angular/router';

@Component({
  standalone: true,
  selector: 'app-product-form',
  imports: [
    CommonModule,
    FormsModule,
  ],
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

  constructor(
    private productService: ProductService,
    private router: Router
  ) {}
ngOnInit(): void {
  let stateProduto: Product | null = null;

  try {
    stateProduto = history.state?.produto;
  } catch (e) {
    console.warn('Não foi possível acessar history.state:', e);
  }

  if (stateProduto && stateProduto.id) {
    this.product = { ...stateProduto };
  } else {
    this.resetarFormulario();
  }

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

   const preco = typeof this.product.price === 'string'
  ? parseFloat(
      String(this.product.price)
        .replace(/\./g, '')
        .replace(',', '.')
    )
  : this.product.price;

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
      created_at: this.product.created_at || new Date().toISOString()
    };

    if (this.product.id) {
      this.productService.atualizarProduto({ ...novoProduto, id: this.product.id }).subscribe({
        next: () => {
          alert('Produto atualizado!');
          this.resetarFormulario();
          this.router.navigate(['/products']);
        },
        error: (err) => console.error('Erro ao atualizar:', err)
      });
    } else {
      this.productService.salvarProduto(novoProduto).subscribe({
        next: () => {
          alert('Produto cadastrado!');
          this.resetarFormulario();
          this.router.navigate(['/products']);
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

export type UserRole = 'ADMIN' | 'VIEWER';

export type Precio = {
  moneda: string;
  valor: number;
};

export type Producto = {
  id: string;
  codigo: string;
  nombre: string;
  caracteristicas?: string;
  empresaNit: string;
  precios: Precio[];
  categorias: string[];
};

export type Empresa = {
  nit: string;
  nombre: string;
  direccion: string;
  telefono: string;
  productos: Array<{
    id: string;
    codigo: string;
    nombre: string;
  }>;
};

export type Categoria = {
  id: string;
  nombre: string;
};

export type Cliente = {
  id: string;
  nombre: string;
  correo: string;
};

export type OrdenItem = {
  productoId: string;
  productoNombre: string;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
};

export type Orden = {
  id: string;
  fecha: string;
  empresaNit: string;
  cliente: Cliente;
  items: OrdenItem[];
  total: number;
};

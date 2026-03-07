export interface UsuarioCadastroDto {
  nome: string;
  email: string;
  senha: string;
}

export interface LoginRequestDto {
  email: string;
  senha: string;
}

export interface LoginResponseDto {
  token: string;
}

export interface Usuario {
  id?: string;
  nome: string;
  email: string;
  senha?: string;
}

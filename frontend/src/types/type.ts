export enum Gender {
  MALE = 'MALE',
  FEMALE = 'FEMALE',
}

export enum UserType {
  STAFF = 'STAFF',
  ADMIN = 'ADMIN',
}

export type Location = {
  id: string;
  code: string;
  name: string;
};

export interface User {
  id: string;
  staffCode: string;
  firstName: string;
  lastName: string;
  username: string;
  gender: Gender;
  dateOfBirth: string;
  joinedOn: string;
  type: UserType;
  location: Location;
  roles: Type[];
  version: number;
}

export interface UserDetailType {
  dateOfBirth: string;
  fullName: string;
  firstName: string;
  lastName: string;
  gender: Gender;
  location: {
    code: string;
    name: string;
    id: string;
  };
  staffCode: string;
  types: {
    id: string;
    name: string;
  }[];
  joinedOn: string;
  username: string;
  version: number;
}

export interface Auth {
  id: string;
  username: string;
  roles: string[];
  changePasswordRequired: boolean;
  accessToken: string;
}

export type ToastType = 'info' | 'success' | 'error' | 'warning';

export interface RefreshAccessToken {
  accessToken: string;
}

export type CurrentUserInfo = Omit<Auth, 'accessToken'>;

export interface UserTableItem {
  id: string;
  staffCode: string;
  fullName: string;
  username: string;
  joinedDate: string;
  type: Type[];
}

export interface Pageable {
  pageNumber: number;
  pageSize: number;
  offset: number;
  numberOfElements: number;
  totalElements: number;
  totalPages: number;
  sorted: boolean;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface UserListResponse {
  content: UserTableItem[];
  pageable: Pageable;
}

export interface Type {
  id: string;
  name: string;
}

export type SortType<T> = {
  key: keyof T;
  order: 'asc' | 'desc';
};

export interface Page<T> {
  content: T[];
  pageable: Pageable;
}

export type BlobData = {
  fileName: string;
  blob: Blob;
};

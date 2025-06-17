export type Response<T> = {
  success: boolean;
  message: string;
  error?: ErrorWrapper;
  data?: T;
};

export type ApiResponse<Success extends boolean, Data = unknown, DetailError = unknown> = Success extends true ?
  ExpectedResponse<Data>: ErrResponse<DetailError>;


export type ExpectedResponse<Data> = {
  message: string;
  data: Data;
}

export type ErrResponse<DetailError = unknown> = {
  message: string;
  error:{
    message: string;
    code: string;
    details?: DetailError;
  }
}

export type ErrorResponse = {
  success: false;
  message: string;
  error: ErrorWrapper;
};

export type ApiErrorResponse<DetailError = never> = {
  success: false;
  message: string;
  error: {
    message: string;
    code: string;
    details?: DetailError
  }
};




export type ErrorWrapper = {
  message: string;
  code: string;
  details?: FieldError[];
};

export type FieldError = {
  field: string;
  message: string;
};

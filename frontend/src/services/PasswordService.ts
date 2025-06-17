import { authApi } from "@/configs/axios";
import { Response } from "@/types/dto";
import { handleApiError } from "@/utils/api";
import { ChangePasswordRequest } from "@/utils/form/schemas/change-password-schema";

export const passwordService = {
  changePassword: async (request: ChangePasswordRequest): Promise<Response<void>> => {
    try {
      const response = await authApi.put<Response<void>>(`/auth/change-password`, request);
      return response.data;
    } catch (error : unknown) {
      return handleApiError(error);
    }
  },
};


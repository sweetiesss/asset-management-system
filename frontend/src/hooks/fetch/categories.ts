import { CategoryService } from "@/services/CategoryService"
import { useGet } from "."
import { Category } from "@/types/asset"

export const useGetCategories = () => {
    return useGet<Category[]>("/categories", CategoryService.getCategoryList)
}
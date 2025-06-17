import { FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import capitalizeEachWord from "@/utils/capitilizeUtils"; 
import { ReportEntity } from "@/types/report";
import type { FieldValues, Control, Path } from "react-hook-form";

type EntityFieldProps<T extends FieldValues> = {
  name: Path<T>;
  control: Control<T>;
  label?: string;
  inline?: boolean;
};

export const EntityField = <T extends FieldValues>({
  name,
  control,
  label,
  inline = false,
}: EntityFieldProps<T>) => {
  return (
    <FormField
      control={control}
      name={name}
      render={({ field }) => (
        <FormItem className={`space-y-3 ${inline ? "flex items-center" : ""}`}>
          <FormLabel>{label || "Report Type"}</FormLabel>
          <FormControl>
            <RadioGroup
              onValueChange={field.onChange}
              className="flex gap-6"
              value={field.value}
              defaultValue={ReportEntity.ASSETS}
            >
              <div className="flex items-center space-x-2">
                <RadioGroupItem value={ReportEntity.USERS} id="users" />
                <label
                  htmlFor="users"
                  className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70"
                >
                  {capitalizeEachWord(ReportEntity.USERS.toString())}
                </label>
              </div>
              <div className="flex items-center space-x-2">
                <RadioGroupItem value={ReportEntity.ASSETS} id="assets" />
                <label
                  htmlFor="assets"
                  className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70"
                >
                  {capitalizeEachWord(ReportEntity.ASSETS.toString())}
                </label>
              </div>
              <div className="flex items-center space-x-2">
                <RadioGroupItem value={ReportEntity.ASSIGNMENTS} id="assignments" />
                <label
                  htmlFor="assignments"
                  className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70"
                >
                  {capitalizeEachWord(ReportEntity.ASSIGNMENTS.toString())}
                </label>
              </div>
            </RadioGroup>
          </FormControl>
          <FormMessage />
        </FormItem>
      )}
    />
  );
};

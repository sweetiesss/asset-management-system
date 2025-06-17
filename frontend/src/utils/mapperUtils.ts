import { User, UserTableItem } from "@/types/type";

export const convertUserToUserTableItem = (user: User): UserTableItem => {
    return {
        id: user.id,
        staffCode: user.staffCode,
        fullName: `${user.firstName} ${user.lastName}`,
        username: user.username,
        joinedDate: user.joinedOn,
        type: user.roles,
    };
}

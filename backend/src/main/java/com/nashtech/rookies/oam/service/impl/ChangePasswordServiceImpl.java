package com.nashtech.rookies.oam.service.impl;

import com.nashtech.rookies.oam.dto.request.ChangePasswordRequest;
import com.nashtech.rookies.oam.exception.OldPasswordNotMatchException;
import com.nashtech.rookies.oam.exception.OldPasswordNullException;
import com.nashtech.rookies.oam.exception.PasswordUnchangedException;
import com.nashtech.rookies.oam.exception.UserNotFoundException;
import com.nashtech.rookies.oam.model.CustomUserDetails;
import com.nashtech.rookies.oam.model.User;
import com.nashtech.rookies.oam.model.enums.RoleName;
import com.nashtech.rookies.oam.model.enums.UserStatus;
import com.nashtech.rookies.oam.repository.UserRepository;
import com.nashtech.rookies.oam.service.ChangePasswordService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChangePasswordServiceImpl implements ChangePasswordService {
    private final String USER_NOT_FOUND = "User not found with id: ";
    private final String OLD_PASSWORD_NOT_MATCH = "Password is incorrect";
    private final String NEW_PASSWORD_MUST_BE_DIFFERENT="New password must be different from old password";
    private final String OLD_PASSWORD_NULL = "Old password must be provided if not first login";
    private final String UNAUTHORIZED_PASSWORD_CHANGE = "You are not allowed to change password for this user";
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {

        validateUserHasCorrectIdOrIsAdmin(request.getUserId());

        User user = userRepository.findById(request.getUserId()).orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND + request.getUserId()));
        if (user.getStatus() == UserStatus.ACTIVE){
            if (StringUtils.isBlank(request.getOldPassword())) {
                throw new OldPasswordNullException(OLD_PASSWORD_NULL);
            }
            if (!passwordEncoder.matches(request.getOldPassword(), user.getHashedPassword())) {
                throw new OldPasswordNotMatchException(OLD_PASSWORD_NOT_MATCH);
            }
            if (passwordEncoder.matches(request.getNewPassword(), user.getHashedPassword())) {
                throw new PasswordUnchangedException(NEW_PASSWORD_MUST_BE_DIFFERENT);
            }
            user.setHashedPassword(passwordEncoder.encode(request.getNewPassword()));
        }else if(user.getStatus()==UserStatus.FIRST_LOGIN){
            user.setStatus(UserStatus.ACTIVE);
            user.setHashedPassword(passwordEncoder.encode(request.getNewPassword()));
        }
        try {
            userRepository.save(user);
        }catch (OptimisticLockingFailureException e){
            throw new OptimisticLockingFailureException(OLD_PASSWORD_NOT_MATCH);
        }

    }
    private void validateUserHasCorrectIdOrIsAdmin(UUID id){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails authUser = (CustomUserDetails) authentication.getPrincipal();
        User authUserEntity = authUser.getUser();
        if(!isAdmin(authentication)) {
            if (!authUserEntity.getId().equals(id)) {
                throw new AccessDeniedException(UNAUTHORIZED_PASSWORD_CHANGE);
            }
        }
    }
    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_"+RoleName.ADMIN.name()));
    }
}

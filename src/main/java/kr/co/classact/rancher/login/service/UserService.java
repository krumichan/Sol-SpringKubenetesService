package kr.co.classact.rancher.login.service;

import kr.co.classact.rancher.login.domain.User;
import kr.co.classact.rancher.login.domain.UserDto;
import kr.co.classact.rancher.login.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * User 를 Database 에 저장.
     */
    @Transactional
    public Long save(UserDto userDto) {

        // 중복 검증
        validateDuplicationUser(userDto);

        User user = User.builder()
                .name(userDto.getName())
                .email(userDto.getEmail())
                .password(new BCryptPasswordEncoder().encode(userDto.getPassword()))
                .auth(userDto.getAuth())
                .build();
        userRepository.save(user);
        return user.getCode();
    }

    /**
     * Spring Security 필수 메소드.
     * @param email 이메일.
     * @return 유저 정보.
     * @throws UsernameNotFoundException 유저가 없을 경우 발생 예외.
     */
    @Override
    public User loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(
                        () -> new UsernameNotFoundException((email))
                );
    }

    /**
     * User 중복 검증.
     */
    private void validateDuplicationUser(UserDto userDto) {
        userRepository.findByEmail(userDto.getEmail())
            .ifPresent(
                    u -> { throw new IllegalStateException("already exists..."); }
            );
    }
}

/*
 * line 40
 *      # 본래 반환타입은 'UserDetails' 지만,
 *      # 여기서는 'UserDetails' 를 상속받은 User 를 반환.
 */
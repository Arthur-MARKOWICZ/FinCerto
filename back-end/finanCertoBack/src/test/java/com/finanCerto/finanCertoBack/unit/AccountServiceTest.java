package com.finanCerto.finanCertoBack.unit;

import com.finanCerto.finanCertoBack.account.AccountType;
import com.finanCerto.finanCertoBack.account.Account;
import com.finanCerto.finanCertoBack.account.AccountRepository;
import com.finanCerto.finanCertoBack.account.AccountService;
import com.finanCerto.finanCertoBack.account.dtos.AccountRegistrationDto;
import com.finanCerto.finanCertoBack.account.dtos.AccountResponseDto;
import com.finanCerto.finanCertoBack.exception.AccountAlreadyExistsException;
import com.finanCerto.finanCertoBack.exception.AccountNotFoundException;
import com.finanCerto.finanCertoBack.user.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AccountServiceTest {
    @InjectMocks
    private AccountService accountService;
    @Mock
    private AccountRepository accountRepository;

    private User userSetup;
    private Account accountSetup;
    private MockedStatic<SecurityContextHolder> mockedSecurityContextHolder;

    @BeforeEach
    void setup() {
        userSetup = new User();
        userSetup.setId(1L);
        userSetup.setName("setup");
        userSetup.setEmail("setup@test.com");
        userSetup.setPassword("test");

        accountSetup = new Account();
        accountSetup.setId(10L);
        accountSetup.setName("setup");
        accountSetup.setType(AccountType.CHECKING);
        accountSetup.setInitialBalance(100.00);
        accountSetup.setUser(userSetup);

        // Mock SecurityContextHolder
        mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);

        mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getPrincipal()).thenReturn(userSetup);
    }

    @AfterEach
    void tearDown() {
        mockedSecurityContextHolder.close();
    }

    @Test
    @DisplayName("Should create an account")
    void shouldCreateAccount() {
        when(accountRepository.existsByUserIdAndName(1L, "setup")).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(accountSetup);
        
        AccountRegistrationDto accountRegistrationDto = new AccountRegistrationDto("setup", AccountType.CHECKING, 100.00, "token");
        AccountResponseDto response = accountService.register(accountRegistrationDto);
        
        assertEquals("setup", response.name());
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    @DisplayName("Should prevent creating account with the same name")
    void shouldPreventAccountWithSameName() {
        when(accountRepository.existsByUserIdAndName(1L, "test")).thenReturn(true);
        
        AccountRegistrationDto dto = new AccountRegistrationDto("test", AccountType.CHECKING, 100.00, "token");
        
        assertThrows(AccountAlreadyExistsException.class, () -> {
            accountService.register(dto);
        });
        verify(accountRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get account by name")
    void shouldGetAccountByName() {
        when(accountRepository.findByNameAndUserId(userSetup.getId(), "setup"))
                .thenReturn(Optional.of(accountSetup));

        AccountResponseDto response = accountService.getAccountByName("setup");

        assertEquals("setup", response.name());
    }

    @Test
    @DisplayName("Should get all user accounts")
    void shouldGetAllAccountsByUser() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Account> page = new PageImpl<>(List.of(accountSetup));

        when(accountRepository.findAllByUserId(userSetup.getId(), pageable))
                .thenReturn(page);

        Page<AccountResponseDto> outcome = accountService.getAllByUserId(pageable);

        assertEquals(1, outcome.getTotalElements());
        assertEquals("setup", outcome.getContent().get(0).name());
    }

    @Test
    @DisplayName("Should update account data")
    void shouldUpdateAccount() {
        AccountRegistrationDto dto = new AccountRegistrationDto("newAccount", AccountType.CHECKING, 500.00, "token");

        when(accountRepository.findById(1L)).thenReturn(Optional.of(accountSetup));
        when(accountRepository.save(any(Account.class))).thenAnswer(i -> i.getArgument(0));

        AccountResponseDto outcome = accountService.updateAccount(1L, dto);

        assertEquals("newAccount", outcome.name());
        assertEquals(500.00, outcome.initialBalance());
    }

    @Test
    @DisplayName("Should add balance to account")
    void shouldAddBalance() {
        accountService.addBalance(50.00, accountSetup);
        assertEquals(150.00, accountSetup.getInitialBalance());
    }

    @Test
    @DisplayName("Should decrease balance from account")
    void shouldDecreaseBalance() {
        accountService.subtractBalance(30.00, accountSetup);
        assertEquals(70.00, accountSetup.getInitialBalance());
    }
}

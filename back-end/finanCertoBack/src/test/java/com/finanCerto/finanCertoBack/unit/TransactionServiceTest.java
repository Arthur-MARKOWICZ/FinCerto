package com.finanCerto.finanCertoBack.unit;

import com.finanCerto.finanCertoBack.category.Category;
import com.finanCerto.finanCertoBack.category.CategoryService;
import com.finanCerto.finanCertoBack.account.Account;
import com.finanCerto.finanCertoBack.account.AccountService;
import com.finanCerto.finanCertoBack.exception.TransactionNotFoundException;
import com.finanCerto.finanCertoBack.budget.BudgetService;
import com.finanCerto.finanCertoBack.security.TokenService;
import com.finanCerto.finanCertoBack.transaction.TransactionType;
import com.finanCerto.finanCertoBack.transaction.Transaction;
import com.finanCerto.finanCertoBack.transaction.TransactionRepository;
import com.finanCerto.finanCertoBack.transaction.TransactionService;
import com.finanCerto.finanCertoBack.transaction.dto.TransactionRegistrationDto;
import com.finanCerto.finanCertoBack.transaction.dto.TransactionResponseDto;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.finanCerto.finanCertoBack.account.AccountType.CHECKING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TransactionServiceTest {

    @InjectMocks
    private TransactionService transactionService;
    @Mock
    private TransactionRepository repository;
    @Mock
    private CategoryService categoryService;
    @Mock
    private AccountService accountService;
    @Mock
    private BudgetService budgetService;

    private User user;
    private Category category;
    private Account account;
    private MockedStatic<SecurityContextHolder> mockedSecurityContextHolder;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(1L);
        user.setName("Tester");
        user.setEmail("tester@mail.com");
        user.setPassword("secret");

        category = new Category();
        category.setName("Food");

        account = new Account();
        account.setName("Wallet");
        account.setType(CHECKING);
        account.setInitialBalance(300.0);
        account.setUser(user);

        // Mock SecurityContextHolder
        mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        
        mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
    }

    @AfterEach
    void tearDown() {
        mockedSecurityContextHolder.close();
    }

    @Test
    @DisplayName("Should register expense, decrease balance, update budget and save")
    void shouldRegisterExpense() {
        TransactionRegistrationDto dto = new TransactionRegistrationDto(
                50.0, LocalDateTime.now(), "Grocery", TransactionType.EXPENSE, account.getName(), category.getName(), "token"
        );

        when(accountService.findEntityByName(dto.accountName())).thenReturn(account);
        when(categoryService.findEntityByName(dto.categoryName())).thenReturn(category);
        when(repository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t.setId(1L);
            return t;
        });

        TransactionResponseDto response = transactionService.register(dto);

        assertEquals(TransactionType.EXPENSE, response.type());
        assertEquals("Grocery", response.description());
        verify(accountService).subtractBalance(dto.amount(), account);
        verify(budgetService).add(dto.amount(), category);
        verify(repository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should register income, increase balance, update budget and save")
    void shouldRegisterIncome() {
        TransactionRegistrationDto dto = new TransactionRegistrationDto(
                200.0, LocalDateTime.now(), "Salary", TransactionType.INCOME, account.getName(), category.getName(), "token"
        );

        when(accountService.findEntityByName(dto.accountName())).thenReturn(account);
        when(categoryService.findEntityByName(dto.categoryName())).thenReturn(category);
        when(repository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t.setId(2L);
            return t;
        });

        TransactionResponseDto response = transactionService.register(dto);

        assertEquals(TransactionType.INCOME, response.type());
        assertEquals("Salary", response.description());
        verify(accountService).addBalance(dto.amount(), account);
        verify(repository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should get transaction by id or throw exception when not exists")
    void shouldGetByIdOrThrow() {
        Transaction transaction = new Transaction();
        transaction.setId(10L);
        transaction.setAccount(account);
        transaction.setCategory(category);

        when(repository.findById(10L)).thenReturn(Optional.of(transaction));
        when(repository.findById(99L)).thenReturn(Optional.empty());

        TransactionResponseDto result = transactionService.getById(10L);
        assertEquals(10L, result.id());

        assertThrows(TransactionNotFoundException.class, () -> transactionService.getById(99L));
    }
}

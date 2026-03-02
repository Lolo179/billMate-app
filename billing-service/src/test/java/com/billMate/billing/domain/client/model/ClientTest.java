package com.billMate.billing.domain.client.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ClientTest {

    private static final Long ID = 1L;
    private static final String VALID_NAME = "Acme Corp";
    private static final String VALID_EMAIL = "contact@acme.com";
    private static final String VALID_PHONE = "+34 600 123 456";
    private static final String VALID_NIF = "B12345678";
    private static final String VALID_ADDRESS = "Calle Mayor 1, Madrid";
    private static final OffsetDateTime CREATED_AT = OffsetDateTime.now();

    private Client createValidClient() {
        return new Client(ID, VALID_NAME, VALID_EMAIL, VALID_PHONE, VALID_NIF, VALID_ADDRESS, CREATED_AT);
    }

    @Nested
    @DisplayName("Constructor tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create client with all valid fields")
        void shouldCreateClientWithValidFields() {
            Client client = createValidClient();

            assertEquals(ID, client.getId());
            assertEquals(VALID_NAME, client.getName());
            assertEquals(VALID_EMAIL, client.getEmail());
            assertEquals(VALID_PHONE, client.getPhone());
            assertEquals(VALID_NIF, client.getNif());
            assertEquals(VALID_ADDRESS, client.getAddress());
            assertEquals(CREATED_AT, client.getCreatedAt());
        }

        @Test
        @DisplayName("Should create client with null optional fields")
        void shouldCreateClientWithNullOptionalFields() {
            Client client = new Client(ID, VALID_NAME, VALID_EMAIL, null, VALID_NIF, null, null);

            assertNull(client.getPhone());
            assertNull(client.getAddress());
            assertNull(client.getCreatedAt());
        }
    }

    @Nested
    @DisplayName("Name validation")
    class NameValidation {

        @Test
        @DisplayName("Should throw when name is null")
        void shouldThrowWhenNameIsNull() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> new Client(ID, null, VALID_EMAIL, VALID_PHONE, VALID_NIF, VALID_ADDRESS, CREATED_AT));
            assertEquals("Client name is required", ex.getMessage());
        }

        @Test
        @DisplayName("Should throw when name is blank")
        void shouldThrowWhenNameIsBlank() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> new Client(ID, "   ", VALID_EMAIL, VALID_PHONE, VALID_NIF, VALID_ADDRESS, CREATED_AT));
            assertEquals("Client name is required", ex.getMessage());
        }

        @Test
        @DisplayName("Should throw when name exceeds 255 characters")
        void shouldThrowWhenNameExceeds255() {
            String longName = "A".repeat(256);
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> new Client(ID, longName, VALID_EMAIL, VALID_PHONE, VALID_NIF, VALID_ADDRESS, CREATED_AT));
            assertEquals("Client name must not exceed 255 characters", ex.getMessage());
        }

        @Test
        @DisplayName("Should accept name with exactly 255 characters")
        void shouldAcceptNameWith255Characters() {
            String name255 = "A".repeat(255);
            Client client = new Client(ID, name255, VALID_EMAIL, VALID_PHONE, VALID_NIF, VALID_ADDRESS, CREATED_AT);
            assertEquals(name255, client.getName());
        }

        @Test
        @DisplayName("Should throw when setting null name via setter")
        void shouldThrowWhenSetNameNull() {
            Client client = createValidClient();
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> client.setName(null));
            assertEquals("Client name is required", ex.getMessage());
        }

        @Test
        @DisplayName("Should throw when setting blank name via setter")
        void shouldThrowWhenSetNameBlank() {
            Client client = createValidClient();
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> client.setName(""));
            assertEquals("Client name is required", ex.getMessage());
        }

        @Test
        @DisplayName("Should throw when setting name exceeding 255 via setter")
        void shouldThrowWhenSetNameTooLong() {
            Client client = createValidClient();
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> client.setName("A".repeat(256)));
            assertEquals("Client name must not exceed 255 characters", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Email validation")
    class EmailValidation {

        @Test
        @DisplayName("Should throw when email is null")
        void shouldThrowWhenEmailIsNull() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> new Client(ID, VALID_NAME, null, VALID_PHONE, VALID_NIF, VALID_ADDRESS, CREATED_AT));
            assertEquals("Client email is required", ex.getMessage());
        }

        @Test
        @DisplayName("Should throw when email is blank")
        void shouldThrowWhenEmailIsBlank() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> new Client(ID, VALID_NAME, "  ", VALID_PHONE, VALID_NIF, VALID_ADDRESS, CREATED_AT));
            assertEquals("Client email is required", ex.getMessage());
        }

        @Test
        @DisplayName("Should throw when email format is invalid")
        void shouldThrowWhenEmailFormatIsInvalid() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> new Client(ID, VALID_NAME, "not-an-email", VALID_PHONE, VALID_NIF, VALID_ADDRESS, CREATED_AT));
            assertEquals("Client email format is invalid", ex.getMessage());
        }

        @Test
        @DisplayName("Should throw when email has no domain")
        void shouldThrowWhenEmailHasNoDomain() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Client(ID, VALID_NAME, "user@", VALID_PHONE, VALID_NIF, VALID_ADDRESS, CREATED_AT));
        }

        @Test
        @DisplayName("Should accept valid email formats")
        void shouldAcceptValidEmails() {
            assertDoesNotThrow(() -> new Client(ID, VALID_NAME, "user@domain.com", VALID_PHONE, VALID_NIF, VALID_ADDRESS, CREATED_AT));
            assertDoesNotThrow(() -> new Client(ID, VALID_NAME, "user.name@domain.co.uk", VALID_PHONE, VALID_NIF, VALID_ADDRESS, CREATED_AT));
            assertDoesNotThrow(() -> new Client(ID, VALID_NAME, "user-name@domain.org", VALID_PHONE, VALID_NIF, VALID_ADDRESS, CREATED_AT));
        }

        @Test
        @DisplayName("Should throw when setting invalid email via setter")
        void shouldThrowWhenSetInvalidEmail() {
            Client client = createValidClient();
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> client.setEmail("bad-email"));
            assertEquals("Client email format is invalid", ex.getMessage());
        }

        @Test
        @DisplayName("Should throw when setting null email via setter")
        void shouldThrowWhenSetNullEmail() {
            Client client = createValidClient();
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> client.setEmail(null));
            assertEquals("Client email is required", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("NIF validation")
    class NifValidation {

        @Test
        @DisplayName("Should throw when NIF is null")
        void shouldThrowWhenNifIsNull() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> new Client(ID, VALID_NAME, VALID_EMAIL, VALID_PHONE, null, VALID_ADDRESS, CREATED_AT));
            assertEquals("Client NIF is required", ex.getMessage());
        }

        @Test
        @DisplayName("Should throw when NIF is blank")
        void shouldThrowWhenNifIsBlank() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> new Client(ID, VALID_NAME, VALID_EMAIL, VALID_PHONE, "  ", VALID_ADDRESS, CREATED_AT));
            assertEquals("Client NIF is required", ex.getMessage());
        }

        @Test
        @DisplayName("Should throw when setting null NIF via setter")
        void shouldThrowWhenSetNullNif() {
            Client client = createValidClient();
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> client.setNif(null));
            assertEquals("Client NIF is required", ex.getMessage());
        }

        @Test
        @DisplayName("Should throw when setting blank NIF via setter")
        void shouldThrowWhenSetBlankNif() {
            Client client = createValidClient();
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> client.setNif(""));
            assertEquals("Client NIF is required", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Setters for optional fields")
    class OptionalFieldSetters {

        @Test
        @DisplayName("Should update phone via setter")
        void shouldUpdatePhone() {
            Client client = createValidClient();
            client.setPhone("+34 700 000 000");
            assertEquals("+34 700 000 000", client.getPhone());
        }

        @Test
        @DisplayName("Should allow null phone")
        void shouldAllowNullPhone() {
            Client client = createValidClient();
            client.setPhone(null);
            assertNull(client.getPhone());
        }

        @Test
        @DisplayName("Should update address via setter")
        void shouldUpdateAddress() {
            Client client = createValidClient();
            client.setAddress("Nueva dirección");
            assertEquals("Nueva dirección", client.getAddress());
        }

        @Test
        @DisplayName("Should allow null address")
        void shouldAllowNullAddress() {
            Client client = createValidClient();
            client.setAddress(null);
            assertNull(client.getAddress());
        }

        @Test
        @DisplayName("Should update createdAt via setter")
        void shouldUpdateCreatedAt() {
            Client client = createValidClient();
            OffsetDateTime newDate = OffsetDateTime.now().plusDays(1);
            client.setCreatedAt(newDate);
            assertEquals(newDate, client.getCreatedAt());
        }

        @Test
        @DisplayName("Should update id via setter")
        void shouldUpdateId() {
            Client client = createValidClient();
            client.setId(99L);
            assertEquals(99L, client.getId());
        }
    }
}

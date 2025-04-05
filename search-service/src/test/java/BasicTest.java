import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BasicTest {

    @Test
    public void testSimple() {
        // Arrange: Chuẩn bị dữ liệu
        int a = 5;
        int b = 10;

        // Act: Thực hiện phép toán
        int result = a + b;

        // Assert: Kiểm tra kết quả
        assertEquals(15, result);
    }
}

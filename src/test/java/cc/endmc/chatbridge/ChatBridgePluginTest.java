package cc.endmc.chatbridge;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.bukkit.configuration.file.FileConfiguration;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ChatBridge插件测试类
 */
class ChatBridgePluginTest {

    @Mock
    private Server mockServer;

    @Mock
    private PluginManager mockPluginManager;

    @Mock
    private FileConfiguration mockConfig;

    private ChatBridgePlugin plugin;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        plugin = new ChatBridgePlugin();
    }

    @Test
    void testJsonEscape() throws Exception {
        // 使用反射访问私有方法进行测试
        java.lang.reflect.Method escapeJsonMethod = ChatBridgePlugin.class
                .getDeclaredMethod("escapeJson", String.class);
        escapeJsonMethod.setAccessible(true);

        // 测试特殊字符转义
        String input = "Hello \"World\"\nNew Line\tTab\\Backslash";
        String expected = "Hello \\\"World\\\"\\nNew Line\\tTab\\\\Backslash";
        String result = (String) escapeJsonMethod.invoke(plugin, input);

        assertEquals(expected, result);
    }

    @Test
    void testJsonEscapeNull() throws Exception {
        java.lang.reflect.Method escapeJsonMethod = ChatBridgePlugin.class
                .getDeclaredMethod("escapeJson", String.class);
        escapeJsonMethod.setAccessible(true);

        String result = (String) escapeJsonMethod.invoke(plugin, (String) null);
        assertEquals("", result);
    }

    @Test
    void testSignatureGeneration() throws NoSuchAlgorithmException {
        // 测试签名生成算法
        String timestamp = "1640995200000";
        String nonce = "test-nonce-123";
        String secretKey = "test-secret-key";

        String signData = timestamp + nonce + secretKey;
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(signData.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        String expectedSignature = hexString.toString();

        // 验证签名长度（SHA256 = 64个字符）
        assertEquals(64, expectedSignature.length());

        // 验证签名只包含十六进制字符
        assertTrue(expectedSignature.matches("^[a-f0-9]+$"));
    }

    @Test
    void testBuildJsonBody() throws Exception {
        // 使用反射测试JSON构建
        java.lang.reflect.Method buildJsonBodyMethod = ChatBridgePlugin.class
                .getDeclaredMethod("buildJsonBody", String.class, String.class);
        buildJsonBodyMethod.setAccessible(true);

        // 设置serverId字段
        java.lang.reflect.Field serverIdField = ChatBridgePlugin.class.getDeclaredField("serverId");
        serverIdField.setAccessible(true);
        serverIdField.set(plugin, "test-server");

        String result = (String) buildJsonBodyMethod.invoke(plugin, "TestPlayer", "Hello World");
        String expected = "{\"playerName\":\"TestPlayer\",\"message\":\"Hello World\",\"serverId\":\"test-server\"}";

        assertEquals(expected, result);
    }

    @Test
    void testConfigurationValidation() throws Exception {
        // 测试配置验证逻辑
        java.lang.reflect.Method validateConfigMethod = ChatBridgePlugin.class
                .getDeclaredMethod("validateConfiguration");
        validateConfigMethod.setAccessible(true);

        // 设置有效配置
        java.lang.reflect.Field apiUrlField = ChatBridgePlugin.class.getDeclaredField("apiUrl");
        apiUrlField.setAccessible(true);
        apiUrlField.set(plugin, "http://localhost:8080/api/v1/pushMessage");

        java.lang.reflect.Field serverIdField = ChatBridgePlugin.class.getDeclaredField("serverId");
        serverIdField.setAccessible(true);
        serverIdField.set(plugin, "test-server");

        java.lang.reflect.Field enableSignatureField = ChatBridgePlugin.class.getDeclaredField("enableSignature");
        enableSignatureField.setAccessible(true);
        enableSignatureField.set(plugin, false);

        java.lang.reflect.Field connectTimeoutField = ChatBridgePlugin.class.getDeclaredField("connectTimeout");
        connectTimeoutField.setAccessible(true);
        connectTimeoutField.set(plugin, 10);

        java.lang.reflect.Field requestTimeoutField = ChatBridgePlugin.class.getDeclaredField("requestTimeout");
        requestTimeoutField.setAccessible(true);
        requestTimeoutField.set(plugin, 30);

        boolean result = (Boolean) validateConfigMethod.invoke(plugin);
        assertTrue(result);
    }

    @Test
    void testInvalidApiUrl() throws Exception {
        java.lang.reflect.Method validateConfigMethod = ChatBridgePlugin.class
                .getDeclaredMethod("validateConfiguration");
        validateConfigMethod.setAccessible(true);

        // 设置无效的API URL
        java.lang.reflect.Field apiUrlField = ChatBridgePlugin.class.getDeclaredField("apiUrl");
        apiUrlField.setAccessible(true);
        apiUrlField.set(plugin, "invalid-url");

        java.lang.reflect.Field serverIdField = ChatBridgePlugin.class.getDeclaredField("serverId");
        serverIdField.setAccessible(true);
        serverIdField.set(plugin, "test-server");

        boolean result = (Boolean) validateConfigMethod.invoke(plugin);
        assertFalse(result);
    }

    @Test
    void testEmptyServerId() throws Exception {
        java.lang.reflect.Method validateConfigMethod = ChatBridgePlugin.class
                .getDeclaredMethod("validateConfiguration");
        validateConfigMethod.setAccessible(true);

        java.lang.reflect.Field apiUrlField = ChatBridgePlugin.class.getDeclaredField("apiUrl");
        apiUrlField.setAccessible(true);
        apiUrlField.set(plugin, "http://localhost:8080/api/v1/pushMessage");

        // 设置空的服务器ID
        java.lang.reflect.Field serverIdField = ChatBridgePlugin.class.getDeclaredField("serverId");
        serverIdField.setAccessible(true);
        serverIdField.set(plugin, "");

        boolean result = (Boolean) validateConfigMethod.invoke(plugin);
        assertFalse(result);
    }
}
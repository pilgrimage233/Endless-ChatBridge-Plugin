package cc.endmc.chatbridge;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Minecraft聊天桥接插件
 * 将游戏内聊天消息转发到QQ群
 * 支持签名验证和外置配置
 * 
 * @author Memory
 * @version 2.0
 */
public class ChatBridgePlugin extends JavaPlugin implements Listener {
    
    // 配置项
    private String apiUrl;
    private String serverId;
    private String secretKey;
    private String targetGroups;
    private boolean enableSignature;
    private int connectTimeout;
    private int requestTimeout;
    private boolean enableDebugLog;
    
    // HTTP客户端
    private HttpClient httpClient;

    // JSON处理器
    private Gson gson;
    
    @Override
    public void onEnable() {
        // 保存默认配置文件
        saveDefaultConfig();
        
        // 加载配置
        loadConfiguration();
        
        // 验证配置
        if (!validateConfiguration()) {
            getLogger().severe("配置验证失败，插件将被禁用！");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // 创建HTTP客户端
        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(connectTimeout))
                .build();

        // 创建Gson实例
        gson = new GsonBuilder()
                .disableHtmlEscaping()
                .create();
        
        // 注册事件监听器
        getServer().getPluginManager().registerEvents(this, this);
        
        // 输出启动信息
        getLogger().info("=== ChatBridge插件已启用 ===");
        getLogger().info("API地址: " + apiUrl);
        getLogger().info("服务器ID: " + serverId);
        getLogger().info("目标群组: " + (targetGroups.isEmpty() ? "未配置" : targetGroups));
        getLogger().info("签名验证: " + (enableSignature ? "启用" : "禁用"));
        getLogger().info("连接超时: " + connectTimeout + "秒");
        getLogger().info("请求超时: " + requestTimeout + "秒");
        getLogger().info("调试日志: " + (enableDebugLog ? "启用" : "禁用"));
        getLogger().info("========================");
    }
    
    @Override
    public void onDisable() {
        getLogger().info("ChatBridge插件已禁用！");
    }
    
    /**
     * 加载配置文件
     */
    private void loadConfiguration() {
        reloadConfig();
        
        // API配置
        apiUrl = getConfig().getString("api.url", "http://localhost:8080/api/v1/pushMessage");
        serverId = getConfig().getString("api.server-id", "1");
        targetGroups = getConfig().getString("api.target-groups", "");
        
        // 签名配置
        enableSignature = getConfig().getBoolean("signature.enabled", true);
        secretKey = getConfig().getString("signature.secret-key", "");
        
        // 网络配置
        connectTimeout = getConfig().getInt("network.connect-timeout", 10);
        requestTimeout = getConfig().getInt("network.request-timeout", 30);
        
        // 调试配置
        enableDebugLog = getConfig().getBoolean("debug.enabled", false);
    }
    
    /**
     * 验证配置
     */
    private boolean validateConfiguration() {
        if (apiUrl == null || apiUrl.trim().isEmpty()) {
            getLogger().severe("API地址不能为空！请检查配置文件中的 api.url");
            return false;
        }
        
        if (serverId == null || serverId.trim().isEmpty()) {
            getLogger().severe("服务器ID不能为空！请检查配置文件中的 api.server-id");
            return false;
        }
        
        if (enableSignature && (secretKey == null || secretKey.trim().isEmpty())) {
            getLogger().severe("启用签名验证时，密钥不能为空！请检查配置文件中的 signature.secret-key");
            return false;
        }
        
        if (connectTimeout <= 0 || requestTimeout <= 0) {
            getLogger().severe("超时时间必须大于0！请检查配置文件中的网络配置");
            return false;
        }
        
        try {
            URI.create(apiUrl);
        } catch (Exception e) {
            getLogger().severe("API地址格式错误：" + apiUrl);
            return false;
        }
        
        return true;
    }
    
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String playerName = event.getPlayer().getName();
        String message = event.getMessage();
        
        if (enableDebugLog) {
            getLogger().info("捕获聊天消息: " + playerName + " -> " + message);
        }
        
        // 异步发送消息，避免阻塞主线程
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            sendMessageToAPI(playerName, message);
        });
    }
    
    /**
     * 发送消息到API
     */
    private void sendMessageToAPI(String playerName, String message) {
        try {
            // 构建JSON请求体
            String jsonBody = buildJsonBody(playerName, message);
            
            // 创建HTTP请求构建器
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(requestTimeout))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody));
            
            // 如果启用签名验证，添加签名头
            if (enableSignature) {
                addSignatureHeaders(requestBuilder, jsonBody);
            }
            
            // 构建请求
            HttpRequest request = requestBuilder.build();
            
            if (enableDebugLog) {
                getLogger().info("发送请求到: " + apiUrl);
                getLogger().info("请求体: " + jsonBody);
            }
            
            // 发送请求
            HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
            
            // 处理响应
            handleResponse(response, playerName, message);
            
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "网络连接异常: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            getLogger().log(Level.WARNING, "请求被中断: " + e.getMessage(), e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "发送消息时发生未知异常", e);
        }
    }
    
    /**
     * 构建JSON请求体
     */
    private String buildJsonBody(String playerName, String message) {
        Map<String, String> data = new HashMap<>();
        data.put("playerName", playerName);
        data.put("message", message);
        data.put("serverId", serverId);

        if (targetGroups != null && !targetGroups.trim().isEmpty()) {
            data.put("targetGroups", targetGroups);
        }

        return gson.toJson(data);
    }
    
    /**
     * 添加签名验证头
     */
    private void addSignatureHeaders(HttpRequest.Builder requestBuilder, String requestBody) {
        try {
            // 生成时间戳
            String timestamp = String.valueOf(System.currentTimeMillis());

            // 生成随机数 当前时间戳后三位加随机三位
            String randomPart = String.valueOf((int)(Math.random() * 900) + 100);
            String nonce = timestamp.substring(timestamp.length() - 3) + randomPart;

            // 生成签名
            String signature = generateSignature(timestamp, nonce, requestBody);
            
            // 添加签名头
            requestBuilder.header("X-Timestamp", timestamp);
            requestBuilder.header("X-Nonce", nonce);
            requestBuilder.header("X-Sign", signature);
            
            if (enableDebugLog) {
                getLogger().info("签名信息 - Timestamp: " + timestamp + ", Nonce: " + nonce + ", Sign: " + signature);
            }
            
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "生成签名时发生异常", e);
            throw new RuntimeException("签名生成失败", e);
        }
    }
    
    /**
     * 生成签名
     * 签名算法: SHA256(timestamp + nonce + secretKey)
     */
    private String generateSignature(String timestamp, String nonce, String requestBody) throws NoSuchAlgorithmException {
        String signData = timestamp + nonce + secretKey;
        
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(signData.getBytes(StandardCharsets.UTF_8));
        
        // 转换为十六进制字符串
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        
        return hexString.toString();
    }
    
    /**
     * 处理HTTP响应
     */
    private void handleResponse(HttpResponse<String> response, String playerName, String message) {
        int statusCode = response.statusCode();
        String responseBody = response.body();
        
        if (statusCode == 200) {
            if (enableDebugLog) {
                getLogger().info("消息发送成功: " + playerName + " -> " + message);
                getLogger().info("服务器响应: " + responseBody);
            } else {
                getLogger().info("消息发送成功: " + playerName);
            }
        } else {
            getLogger().warning("消息发送失败！");
            getLogger().warning("状态码: " + statusCode);
            getLogger().warning("响应内容: " + responseBody);
            getLogger().warning("玩家: " + playerName + ", 消息: " + message);
            
            // 根据状态码给出具体的错误提示
            switch (statusCode) {
                case 400:
                    getLogger().warning("请求参数错误，请检查插件配置");
                    break;
                case 401:
                    getLogger().warning("认证失败，请检查API密钥配置");
                    break;
                case 403:
                    getLogger().warning("签名验证失败，请检查密钥和签名算法");
                    break;
                case 429:
                    getLogger().warning("请求频率过高，请稍后重试");
                    break;
                case 500:
                    getLogger().warning("服务器内部错误，请联系管理员");
                    break;
                default:
                    getLogger().warning("未知错误，状态码: " + statusCode);
            }
        }
    }
    
    /**
     * 重载配置命令
     */
    public boolean onCommand(org.bukkit.command.CommandSender sender, 
                           org.bukkit.command.Command command, 
                           String label, 
                           String[] args) {
        if (command.getName().equalsIgnoreCase("chatbridge")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("chatbridge.reload")) {
                    loadConfiguration();
                    if (validateConfiguration()) {
                        sender.sendMessage("§a[ChatBridge] 配置重载成功！");
                        getLogger().info("配置已重载: " + sender.getName());
                    } else {
                        sender.sendMessage("§c[ChatBridge] 配置验证失败，请检查配置文件！");
                    }
                } else {
                    sender.sendMessage("§c[ChatBridge] 你没有权限执行此命令！");
                }
                return true;
            } else if (args.length > 0 && args[0].equalsIgnoreCase("test")) {
                if (sender.hasPermission("chatbridge.test")) {
                    sender.sendMessage("§a[ChatBridge] 正在发送测试消息...");
                    Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                        sendMessageToAPI("TestPlayer", "这是一条测试消息");
                        sender.sendMessage("§a[ChatBridge] 测试消息已发送，请查看控制台日志！");
                    });
                } else {
                    sender.sendMessage("§c[ChatBridge] 你没有权限执行此命令！");
                }
                return true;
            } else {
                sender.sendMessage("§e[ChatBridge] 可用命令:");
                sender.sendMessage("§e/chatbridge reload - 重载配置");
                sender.sendMessage("§e/chatbridge test - 发送测试消息");
                return true;
            }
        }
        return false;
    }
}
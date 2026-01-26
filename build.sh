#!/bin/bash

# ChatBridge 插件构建脚本
# 作者: Memory
# 版本: 2.0

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 项目信息
PROJECT_NAME="ChatBridge"
VERSION="2.0.0"
BUILD_DIR="target"
PLUGIN_JAR="${PROJECT_NAME}-${VERSION}.jar"

echo -e "${BLUE}=== ${PROJECT_NAME} 插件构建脚本 ===${NC}"
echo -e "${BLUE}版本: ${VERSION}${NC}"
echo ""

# 检查Java版本
echo -e "${YELLOW}检查Java版本...${NC}"
if ! command -v java &> /dev/null; then
    echo -e "${RED}错误: 未找到Java，请安装Java 17或更高版本${NC}"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo -e "${RED}错误: Java版本过低，需要Java 17或更高版本${NC}"
    exit 1
fi
echo -e "${GREEN}Java版本检查通过${NC}"

# 检查Maven
echo -e "${YELLOW}检查Maven...${NC}"
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}错误: 未找到Maven，请安装Maven${NC}"
    exit 1
fi
echo -e "${GREEN}Maven检查通过${NC}"

# 清理旧的构建文件
echo -e "${YELLOW}清理旧的构建文件...${NC}"
if [ -d "$BUILD_DIR" ]; then
    rm -rf "$BUILD_DIR"
    echo -e "${GREEN}清理完成${NC}"
fi

# 运行测试
echo -e "${YELLOW}运行单元测试...${NC}"
mvn test
if [ $? -eq 0 ]; then
    echo -e "${GREEN}测试通过${NC}"
else
    echo -e "${RED}测试失败，构建中止${NC}"
    exit 1
fi

# 编译和打包
echo -e "${YELLOW}编译和打包插件...${NC}"
mvn clean package -DskipTests
if [ $? -eq 0 ]; then
    echo -e "${GREEN}构建成功${NC}"
else
    echo -e "${RED}构建失败${NC}"
    exit 1
fi

# 检查生成的JAR文件
if [ -f "$BUILD_DIR/$PLUGIN_JAR" ]; then
    echo -e "${GREEN}插件JAR文件生成成功: $BUILD_DIR/$PLUGIN_JAR${NC}"
    
    # 显示文件大小
    FILE_SIZE=$(du -h "$BUILD_DIR/$PLUGIN_JAR" | cut -f1)
    echo -e "${BLUE}文件大小: $FILE_SIZE${NC}"
    
    # 验证JAR文件内容
    echo -e "${YELLOW}验证JAR文件内容...${NC}"
    if jar tf "$BUILD_DIR/$PLUGIN_JAR" | grep -q "plugin.yml"; then
        echo -e "${GREEN}plugin.yml 文件存在${NC}"
    else
        echo -e "${RED}警告: plugin.yml 文件不存在${NC}"
    fi
    
    if jar tf "$BUILD_DIR/$PLUGIN_JAR" | grep -q "config.yml"; then
        echo -e "${GREEN}config.yml 文件存在${NC}"
    else
        echo -e "${RED}警告: config.yml 文件不存在${NC}"
    fi
    
    if jar tf "$BUILD_DIR/$PLUGIN_JAR" | grep -q "ChatBridgePlugin.class"; then
        echo -e "${GREEN}主类文件存在${NC}"
    else
        echo -e "${RED}错误: 主类文件不存在${NC}"
        exit 1
    fi
    
else
    echo -e "${RED}错误: 插件JAR文件生成失败${NC}"
    exit 1
fi

# 创建发布目录
RELEASE_DIR="release"
if [ ! -d "$RELEASE_DIR" ]; then
    mkdir -p "$RELEASE_DIR"
fi

# 复制文件到发布目录
cp "$BUILD_DIR/$PLUGIN_JAR" "$RELEASE_DIR/"
cp "README.md" "$RELEASE_DIR/"
cp "config.yml" "$RELEASE_DIR/config-example.yml"

echo -e "${GREEN}文件已复制到发布目录: $RELEASE_DIR/${NC}"

# 生成校验和
echo -e "${YELLOW}生成文件校验和...${NC}"
cd "$RELEASE_DIR"
sha256sum "$PLUGIN_JAR" > "${PLUGIN_JAR}.sha256"
echo -e "${GREEN}校验和文件生成: ${PLUGIN_JAR}.sha256${NC}"
cd ..

# 构建完成
echo ""
echo -e "${GREEN}=== 构建完成 ===${NC}"
echo -e "${GREEN}插件文件: $RELEASE_DIR/$PLUGIN_JAR${NC}"
echo -e "${GREEN}配置示例: $RELEASE_DIR/config-example.yml${NC}"
echo -e "${GREEN}使用说明: $RELEASE_DIR/README.md${NC}"
echo -e "${GREEN}校验和: $RELEASE_DIR/${PLUGIN_JAR}.sha256${NC}"
echo ""
echo -e "${BLUE}安装说明:${NC}"
echo -e "${BLUE}1. 将 $PLUGIN_JAR 复制到服务器的 plugins 目录${NC}"
echo -e "${BLUE}2. 启动服务器生成默认配置${NC}"
echo -e "${BLUE}3. 编辑 plugins/ChatBridge/config.yml 配置文件${NC}"
echo -e "${BLUE}4. 使用 /chatbridge reload 重载配置${NC}"
echo ""
echo -e "${GREEN}构建脚本执行完成！${NC}"
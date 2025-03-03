plugins {
    val kotlinVersion = "1.8.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.16.0"
}

group = "top.jie65535"
version = "1.0.0"

repositories {
    maven("https://maven.aliyun.com/repository/public") // 阿里云国内代理仓库
    mavenCentral()
}

dependencies {
    implementation("net.objecthunter:exp4j:0.4.8")
}
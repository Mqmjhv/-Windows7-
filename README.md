# Win7Lite

一个 **Windows 7 风格精简版模拟器**（Android 原生应用），自带开机动画、桌面、开始菜单，以及一个可拖动、可缩放的**命令提示符（cmd）窗口**，窗口内部运行真正的 Linux shell 终端模拟器。

> 纯 Java + Android SDK，无任何第三方依赖，可直接用 `build.sh` 构建。

## 功能

| 模块 | 说明 |
|------|------|
| 🎬 开机动画 | 四色光点汇聚成 Win7 徽标 + "正在启动 Windows 7" |
| 🖥️ 桌面 / 开始菜单 / 任务栏 | 蓝天壁纸、桌面图标、可展开的开始菜单 |
| 📁 打开文件夹 | 内嵌文件浏览 |
| 🌐 网页浏览 | 内嵌浏览器 |
| 💻 命令提示符 | cmd 风格窗口，可拖动（标题栏）+ 可缩放（右下角手柄）+ 可最大化 |
| 🔄 Windows 命令翻译层 | `dir` / `ipconfig` / `type` / `del` / `ping` / `tasklist` 等十余条命令映射到 Linux 命令 |
| 🐧 底层 shell | 真正 Linux（Android 内核，`sh -c` 执行） |

命令提示符高度还原真实 Windows 7 cmd：

- 窗口标题：`C:\Windows\system32\cmd.exe`
- 启动横幅：`Microsoft Windows [版本 6.1.7601]`（Windows 7 = NT 6.1）
- 版权行：`版权所有 (c) 2009 Microsoft Corporation。保留所有权利。`
- 默认提示符：`C:\Users\Administrator>`
- 配色：黑底（RGB 0,0,0）+ 浅灰字（RGB 192,192,192）

## 支持的命令

| Windows 命令 | 实际执行（Linux） | 说明 |
|---|---|---|
| `dir` | `ls -la` | 列出目录 |
| `type 文件` | `cat` | 查看文件 |
| `del` / `erase` | `rm` | 删除文件 |
| `md` / `mkdir` | `mkdir` | 建目录 |
| `rd` / `rmdir` | `rmdir` | 删目录 |
| `copy` | `cp` | 复制 |
| `move` / `ren` / `rename` | `mv` | 移动/重命名 |
| `ipconfig` | `ifconfig` / `ip addr` | 查看网络 |
| `ping` | `ping -c 4` | 网络探测 |
| `tasklist` | `ps -ef` | 进程列表 |
| `systeminfo` | `uname -a` | 系统信息 |
| `hostname` | `getprop ro.product.model` | 设备名 |
| `wmic` | `cat /proc/cpuinfo` | CPU 信息 |
| `ver` | 内置 | 版本 |
| `cls` / `clear` | 内置 | 清屏 |
| `help` | 内置 | 帮助 |
| `exit` | 内置 | 退出 |

原生 Linux 命令（`ls` / `pwd` / `cat` / `echo` / `uname` / `whoami` / `id` / `date` / `df` / `ps` / `free`）直接透传。

未知命令会显示经典报错：

```
'xxx' 不是内部或外部命令，也不是可运行的程序或批处理文件。
```

## 构建

环境要求：

- Android SDK（`/opt/android-sdk`，含 build-tools 34.0.0）
- JDK 8+（`javac -source 1.8 -target 1.8`）
- `d8`、`apksigner`、`aapt2`、`zipalign`（其中 `aapt2`/`zipalign` 为 x86_64 ELF，需 `qemu-x86_64-static` 前缀调用）

```bash
bash build.sh
```

产物为 `Win7Lite.apk`。

### 构建流程

aapt2 compile → aapt2 link（生成 R.java 和 base.apk）→ javac → d8 转 dex → zip 塞入 dex → zipalign 对齐 → apksigner 签名。

## 项目结构

```
Win7Lite/
├── app/src/main/
│   ├── AndroidManifest.xml
│   ├── java/com/win7lite/
│   │   ├── BootActivity.java        # 开机动画
│   │   ├── MainActivity.java        # 桌面/开始菜单/任务栏
│   │   ├── BrowserActivity.java     # 内嵌浏览器
│   │   └── TerminalActivity.java    # 命令提示符窗口
│   └── res/
│       ├── mipmap/ic_launcher.xml
│       └── values/ (colors/strings/styles)
└── build.sh
```

## 技术说明

- 包名：`com.win7lite`
- 命令提示符采用「透明 Activity + 半透明遮罩 + 可定位窗口容器」方案模拟浮动窗口（Android 中 Activity 本身全屏，非真正的多窗口系统）。
- 终端底层为 Android Linux 内核 shell，通过 `ProcessBuilder` 调用 `sh -c`。

## 许可

本项目仅用于学习与模拟展示，Windows 7 相关名称、徽标、文案版权归 Microsoft Corporation 所有。

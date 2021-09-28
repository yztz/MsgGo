# MsgGo
![MsgGo](./app/src/main/res/drawable/icon.png)  


安卓平台上的一款轻量的短信群发App。  
## Features
* 界面美观，交互性良好
* 基于excel数据格式导入
* 自动获取短信变量名
* 提供短信编辑器，支持短信**变量**代换
* 即时回馈信息发送状态，成功与否，尽在眼前
* 无需指定固定格式，App内指定号码列
* *第三方应用数据一键导入*  

## 基本使用
1. 导入数据
2. 编辑短信内容
3. 选择号码变量
4. 选择收件人号码列
5. 发送

## Excel格式要求
列名|列名|列名|...
-|-|-|-
数据|数据|数据|...
数据|数据|数据|...
...|  
  
注：
1. （未测试）兼容所有excel格式
2. ***发送延迟不要过短，否则可能存在拦截问题***

## 下载
[release](https://github.com/yztz/MsgGo/releases/download/1.5/app.apk)  

## 未来更新计划

* 号码列记忆功能，不用每次发送都要重新选择了。
* 发送情况详单，了解到底是谁没收到短信。
* 考虑增加停止发送按键（或者一旦发送失败则停止接下来的发送功能设置按钮，亡羊补牢为时不晚）
* ~~鉴于时间有限且技术不足，目前暂且不支持第三方文件浏览器打开excel，否则会出现闪退情况，未来考虑会支持。同时也会考虑支持在其他软件中用MsgGo直接导入excel~~（***目前已经支持第三方excel数据文件最直接导入：第三方应用使用选择其他应用打开，选择MsgGo即可***）

如果帮助到您，请赐予一颗小星星吧:)  

e-mail: 19052129@hdu.edu.cn

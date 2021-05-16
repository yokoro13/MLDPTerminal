# MLDP-Terminal
Microchip社のRN4020 Bluetooth Low Energy Moduleを通じてAndtoid上で動作するターミナルエミュレータ．  
通信にはMLDP(Microchip Low Energy Data Profile)を使用する．　　

##  RN4020 初期化
TeC7b(Tokuyama Educational Computer Ver.7b)に搭載されたRN4020は，ボード上のジャンパを二本横差しすることで工場出荷時までリセットすることができる．

## RN4020 初期設定
1. Macにシリアル通信ソフトCoolTermをインストールする．(screenコマンドでも可能)  
2. TeC7bのジャンパをDEMO1に設定し，MacとUSBケーブルで接続する．　　
3. CoolTermを起動し，以下のように設定する．
  <pre>  [options] → [Serial Port] → [Port] → TeC7bを接続しているUSB Serialポートを選択
  [options] → [Terminal] → [Enter Key Emulation] → CRを選択  
  
  ※ TeC7bのファームウェアが最新版(2018/12/1時点)でない場合はさらに
      [options] → [Serial Port] → [Baudrate] → 11520を選択
    の設定が必要になる．</pre>
4. CoolTermに'+'とEnterキーを入力する．Echo Onと出力されれば，接続成功．
5. CoolTermに以下のコマンドを打ち込む．
  <pre>  SF,2           // 工場出荷状態までリセット
  SR,32104C00    // RN4020起動時に自動アドバタイズおよびMLDPモードとして動作するように設定
  R,1            // 再起動
  
  ※ TeC7bのファームウェアが最新版(2018/12/1時点)でない場合は再起動の前に
      SB,1       // ボーレートを9600に設定
    を実行する．</pre>  
  ※ RN4020が正常に動作していればSF，SRに対して`AOK`と返ってくる． `R,1`の実行後，CMDが表示されると設定が終了する．
  
## 使い方
TeC7bに搭載されているTacOSのターミナルとして使用する． また，FreeBSDなどCUIのOSとTeC7bを接続，設定することで各OSのターミナルとしても使用できる．前回接続したBluetoothモジュールには自動で接続することができる．

## 使用可能なエスケープシーケンス 
|記法|内容|
|:---:|:---|
|\e[**n**A|カーソルを上に**n**移動|
|\e[**n**B|カーソルを下に**n**移動|
|\e[**n**C|カーソルを右に**n**移動|
|\e[**n**D|カーソルを左に**n**移動|
|\e[**n**E|カーソルを**n**行下の先頭に移動|
|\e[**n**F|カーソルを**n**行上の先頭に移動|
|\e[**n**G|現在位置と関係なくカーソルを左から**n**の場所に移動|
|\e[**n**;**m**H <br> \e[**n**;**m**f|現在位置と関係なくカーソルを上から**n**，左から**m**の場所に移動|
|\e[**n**H|画面消去 <br> 0 … カーソルより後ろを消去 <br> 1 … カーソルより前を消去 <br> 2 … 画面全体を消去|
|\e[**n**K|行消去 <br> 0 … カーソルより後ろを消去 <br> 1 … カーソルより前を消去 <br> 2 … 行全体を消去|
|\e[**n**m|文字色を変更 <br> 30 … 黒 <br> 31 … 赤 <br> 32 … 緑 <br> 33 … 黄 <br> 34 … 青 <br> 35 … マゼンタ <br> 36 … シアン <br> 37 … 白|

## TERMCAP設定
<pre> tec7b-term:\
     :do=2\E[B:cl=50\E[H\E[J:\
     :le=2\E[D:bs:cm=5\E[%i%d;%dH:nd=2\E[C:up=2\E[A:\
     :ce=3\E[K:cd=50\E[J:\:nw=2\E[E:ho=E[H:\
     :UP=2\E[%DA:DO=2\E[%dB:RI=2\E[%dC:\
     :LE=2\E[%dD:cr=\E[G:\
     :ac=``aaffggjjkkllmmnnooppqqrrssttuuvvwwxxyuzz{{||:\
     :ku=\EOA:kd=\EOB:kr=EOC:kl=\EOD:kb=\177:\
     :k0=\EOy:k1=\EOP:k2=\EOQ:k3=\EOR:k4=\EOS:k5=\EOt:\
     :k6=\EOu:k7=\EOv:k8=\E0l:k9=\EOw:k;=\EOx:@8=\EOM:\
     :K1=\EOq:K2=\EOr:K3=\EOs:K4=\EOp:K5=\EOn:pt:xn:\</pre>
                  
※ FreeBSD 11.2-RELEASE での動作確認時の設定
 
## ttys設定
<pre> ttyU0   "/usr/libexec/getty 3wire.9600" tec7b-term   on secure</pre>

※ FreeBSD 11.2-RELEASE での動作確認時の設定


## 動作環境
android7.0以上

## iOS版
https://github.com/yokoro13/MLDPTerminal-iOS

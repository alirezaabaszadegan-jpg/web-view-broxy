import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() => runApp(const BimehApp());

class BimehApp extends StatelessWidget {
  const BimehApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      builder: (context, child) =>
          Directionality(textDirection: TextDirection.rtl, child: child!),
      home: const LoginPage(),
    );
  }
}

// کانال ارتباط با کد اندروید (پروکسی و WebView)
const channel = MethodChannel('bimeh/web');

// دکمه‌های سایت‌ها - هر وقت خواستید اضافه/کم کنید
const sites = <String, String>{
  'نمایندگان': 'https://agents.iraninsurance.ir',
  'مرکزی': 'https://co.iraninsurance.ir',
};

class LoginPage extends StatefulWidget {
  const LoginPage({super.key});

  @override
  State<LoginPage> createState() => _LoginPageState();
}

class _LoginPageState extends State<LoginPage> {
  final _host = TextEditingController(text: '192.168.100.110');
  final _port = TextEditingController(text: '3128');
  final _user = TextEditingController();
  final _pass = TextEditingController();
  String? _error;

  Future<void> _login() async {
    final h = _host.text.trim();
    final p = _port.text.trim();
    final u = _user.text.trim();
    final pw = _pass.text;
    if (h.isEmpty || p.isEmpty || u.isEmpty || pw.isEmpty) {
      setState(() => _error = 'همه فیلدها را پر کنید');
      return;
    }
    final ok = await channel.invokeMethod<bool>('setProxy', {'hostPort': '$h:$p'}) ?? false;
    if (!ok) {
      setState(() => _error = 'این نسخه WebView از پروکسی پشتیبانی نمی‌کند');
      return;
    }
    if (!mounted) return;
    Navigator.of(context).pushReplacement(MaterialPageRoute(
      builder: (_) => BrowserPage(user: u, pass: pw),
    ));
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('ورود به سامانه بیمه')),
      body: Padding(
        padding: const EdgeInsets.all(20),
        child: ListView(children: [
          TextField(controller: _host, decoration: const InputDecoration(labelText: 'آدرس پروکسی')),
          TextField(controller: _port, decoration: const InputDecoration(labelText: 'پورت'), keyboardType: TextInputType.number),
          TextField(controller: _user, decoration: const InputDecoration(labelText: 'نام کاربری')),
          TextField(controller: _pass, decoration: const InputDecoration(labelText: 'رمز عبور'), obscureText: true),
          const SizedBox(height: 16),
          if (_error != null) Text(_error!, style: const TextStyle(color: Colors.red)),
          ElevatedButton(onPressed: _login, child: const Text('ورود')),
        ]),
      ),
    );
  }
}

class BrowserPage extends StatelessWidget {
  final String user;
  final String pass;
  const BrowserPage({super.key, required this.user, required this.pass});

  Future<void> _logout(BuildContext context) async {
    await channel.invokeMethod('clear');
    if (context.mounted) {
      Navigator.of(context).pushReplacement(MaterialPageRoute(builder: (_) => const LoginPage()));
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Row(
          children: sites.entries
              .map((e) => TextButton(
                    onPressed: () => channel.invokeMethod('load', {'url': e.value}),
                    child: Text(e.key, style: const TextStyle(color: Colors.white)),
                  ))
              .toList(),
        ),
        actions: [
          IconButton(icon: const Icon(Icons.logout), tooltip: 'خروج', onPressed: () => _logout(context)),
        ],
      ),
      body: AndroidView(
        viewType: 'bimeh/webview',
        creationParams: <String, dynamic>{
          'user': user,
          'pass': pass,
          'url': sites.values.first,
        },
        creationParamsCodec: const StandardMessageCodec(),
      ),
    );
  }
}

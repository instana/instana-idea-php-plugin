<?php

<warning descr="Instana PHP SDK is missing in your root composer.json
This could lead to problems in environments, where the <pre>instana</pre> native
PHP extension is not loaded yet. Adding the SDK to your composer manifest will make
sure you dont run into any issues.">\Instana\Tracer</warning>::setServiceName('My cool Service');

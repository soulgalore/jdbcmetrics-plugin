# JDBCMetrics plugin for Jenkins [![Build Status](https://travis-ci.org/soulgalore/jdbcmetrics-plugin.png?branch=master)](https://travis-ci.org/soulgalore/jdbcmetrics-plugin)

Add https://github.com/soulgalore/jdbcmetrics to your Java webapp. Turn on metrics by response headers, that will return the number of database reads & database writes a page generates. Install the JDBCMetrics plugin in Jenkins and you can now see the number of database queries generated for every build in your CI.


## How to build
Build plugin 
<pre>
mvn install
</pre>

## How to test locally (port 8090 because I like it)
<pre>
mvn hpi:run -Djetty.port=8090
</pre>


## License

Copyright 2013 Peter Hedenskog

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

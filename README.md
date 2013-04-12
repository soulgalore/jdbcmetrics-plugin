# JDBCMetrics plugin for Jenkins [![Build Status](https://travis-ci.org/soulgalore/jdbcmetrics-plugin.png?branch=master)](https://travis-ci.org/soulgalore/jdbcmetrics-plugin)

Add https://github.com/soulgalore/jdbcmetrics to your Java webapp. Turn on metrics by response headers, that will return the number of database reads & database writes a page generates. Install the JDBCMetrics plugin in Jenkins and you can now see the number of database queries generated for every build in your CI.




## How it works

### Build the plugin
<pre>
mvn install
</pre>

### Install the plugin in Jenkins
<img src="https://raw.github.com/soulgalore/jdbcmetrics-plugin/master/resources/jenkins-install.png">

### Configure the plugin
<img src="https://raw.github.com/soulgalore/jdbcmetrics-plugin/master/resources/jdbcmetric-jenkins.png">

### When you run the plugin, three files will be created
<img src="https://raw.github.com/soulgalore/jdbcmetrics-plugin/master/resources/jenkins-files.png">

### Setup post actions to take care of the files
<ul>
<li>The Jenkins built in <strong>Publish JUnit test result report</strong> will use the  <i>jdbcmetrics-junit.xml</i> file to publish your test report.</li>
<li><a href="https://wiki.jenkins-ci.org/display/JENKINS/HTML+Publisher+Plugin" target="_blank">Publish HTML reports</a> will publish the result as a nice HTML report, using <i>jdbcmetrics.html</i>.</li>
<li><a href="https://wiki.jenkins-ci.org/display/JENKINS/Plot+Plugin" target="_blank">Plot build plugin</a> will help you build graphs between builds using the <i>jdbcmetrics.xml</i> file.</li>
</ul>


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

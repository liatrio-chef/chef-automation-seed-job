folder('chef')
// job("chef/" + unitTestJobName)
// add views via dsl and not config xml

def cookbooks = ["jenkins-as-code","jenkins-liatrio","nexus-liatrio","archiva-liatrio","apache2-liatrio","hygieia-liatrio","tomcat-liatrio","selenium-liatrio","sonarqube-liatrio","hygieia-dev-unbaked","hygieia-petclinic-demo-unbaked"]

cookbooks.each {
    cookBookName->

    def repoUrl = "https://github.com/liatrio-chef/" + cookBookName + ".git"
    def projectUrl = "https://github.com/liatrio-chef/" + cookBookName
    def unitTestJobName = "chef-cookbook-" + cookBookName + "-1-unit-test"
    def testKitchenJobName = "chef-cookbook-" + cookBookName + "-2-test-kitchen"
    def knifeUploadJobName = "chef-cookbook-" + cookBookName + "-3-knife-upload"

    job("chef/" + unitTestJobName){
        description("This job was created with automation.  Manual edits to this job are discouraged.")
        wrappers {
            colorizeOutput()
        }
        properties {
            githubProjectUrl(projectUrl)
        }
        scm {
            git{
                // branch("master")
                remote {
                    url(repoUrl)
                }
            }
        }
        triggers {
            // scm('H/2 * * * *') // use github hook to triiger
            cron('@weekly')
            githubPush()
        }
        steps {
            shell(readFileFromWorkspace("unit_test.sh"))
        }
        publishers {
            downstream(testKitchenJobName, 'SUCCESS')
            slackNotifier {
                notifyFailure(true)
                notifySuccess(true)
                notifyAborted(false)
                notifyNotBuilt(false)
                notifyUnstable(false)
                notifyBackToNormal(true)
                notifyRepeatedFailure(false)
                startNotification(true)
                includeTestSummary(false)
                includeCustomMessage(false)
                customMessage(null)
                buildServerUrl(null)
                sendAs(null)
                commitInfoChoice('AUTHORS_AND_TITLES')
                teamDomain(null)
                authToken(null)
                room('jenkins-build')
            }
            mailer('drew@liatrio.com', true, false)
            githubCommitNotifier()
        }
    }

    job("chef/" + testKitchenJobName){
        description("This job was created with automation.  Manual edits to this job are discouraged.")
        wrappers {
            colorizeOutput()
        }
        properties {
            githubProjectUrl(projectUrl)
        }
        scm {
            git{
                // branch("master")
                remote {
                    url(repoUrl)
                }
            }
        }
        steps {
            shell("if [ ! -f Berksfile.lock ]; then berks install; else berks update; fi; kitchen test -d always --color")
        }
        publishers {
            downstream(knifeUploadJobName, 'SUCCESS')
            if ( cookBookName == "jenkins-liatrio" )
            {
                downstream("chef-cookbook-jenkins-as-code-1-unit-test", 'SUCCESS')
                downstream("chef-cookbook-hygieia-dev-unbaked-1-unit-test", 'SUCCESS')
                downstream("chef-cookbook-hygieia-petclinic-demo-unbaked-1-unit-test", 'SUCCESS')
            }
          else if ( cookBookName == "hygieia-liatrio" )
            {
                downstream("chef-cookbook-hygieia-dev-unbaked-1-unit-test", 'SUCCESS')
                downstream("chef-cookbook-hygieia-petclinic-demo-unbaked-1-unit-test", 'SUCCESS')
            }
            slackNotifier {
                notifyFailure(true)
                notifySuccess(true)
                notifyAborted(false)
                notifyNotBuilt(false)
                notifyUnstable(false)
                notifyBackToNormal(true)
                notifyRepeatedFailure(false)
                startNotification(true)
                includeTestSummary(true)
                includeCustomMessage(false)
                customMessage(null)
                buildServerUrl(null)
                sendAs(null)
                commitInfoChoice('AUTHORS_AND_TITLES')
                teamDomain(null)
                authToken(null)
                room('jenkins-build')
            }
            mailer('drew@liatrio.com', true, false)
            githubCommitNotifier()
        }
    }

    job("chef/" + knifeUploadJobName){
        //disabled()
        description("This job was created with automation.  Manual edits to this job are discouraged.")
        wrappers {
            colorizeOutput()
            credentialsBinding {
                file('CHEFUPLOADER', 'bf42b6f2-54ab-4172-a24c-48b2bec6737f')
            }
        }
        properties {
            githubProjectUrl(projectUrl)
        }
        scm {
            git{
                branch("master")
                remote {
                    url(repoUrl)
                }
            }
        }
        steps {
            shell('''
cat << EOF > knife.rb
current_dir = File.dirname(__FILE__)
log_level         :info
log_location      STDOUT
node_name         "cloudservices"
client_key        "$CHEFUPLOADER"
chef_server_url   "https://api.chef.io/organizations/liatrio"
cookbook_path     [".."]
#ssl_verify_mode  :verify_none
EOF

''' + """

# knife spork check ${cookBookName} # don't check because it prompts bump

if [ `git log --pretty=oneline | head -1 | grep '#major'` ]
then
  knife spork bump ${cookBookName}

elif [ `git log --pretty=oneline | head -1 | grep '#minor'` ]
then
  knife spork bump ${cookBookName}

elif [ `git log --pretty=oneline | head -1 | grep '#patch'` ]
then
  knife spork bump ${cookBookName}

else
  knife spork bump ${cookBookName}
fi

knife spork upload ${cookBookName}

#knife spork promote sandbox ${cookBookName} --remote""")
        }
        publishers {
            slackNotifier {
                notifyFailure(true)
                notifySuccess(true)
                notifyAborted(false)
                notifyNotBuilt(false)
                notifyUnstable(false)
                notifyBackToNormal(true)
                notifyRepeatedFailure(false)
                startNotification(false)
                includeTestSummary(true)
                includeCustomMessage(false)
                customMessage(null)
                buildServerUrl(null)
                sendAs(null)
                commitInfoChoice('AUTHORS_AND_TITLES')
                teamDomain(null)
                authToken(null)
                room('jenkins-build')
            }
            mailer('drew@liatrio.com', true, false)
            githubCommitNotifier()
        }
    }

}

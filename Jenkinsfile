SERVICE="user"
SCALA_VERSION="2.12"
ASSEMBLY="-assembly"

AMI_VERSION=0
STAGES = "build_artifacts,build_ami,deploy_to_QA"
QA_REGIONS= ["us-west-2","us-east-1"]
PROD_REGIONS= ["ap-southeast-1","eu-west-1","us-west-2","us-east-1"]
common_envs = [ "API=${SERVICE}", "ASSEMBLY=${ASSEMBLY}" ]

buildAMIInfo = [:]

stage('Parameter Check'){
    node{
        TAG = "${params.TAG}"

        try{
            if (TAG == "") {
                throw new Exception("Enter the artifact version for the build in TAG")
            }

            if(TAG.contains("RC") || TAG.contains("SNAPSHOT")){

            }else if(TAG.contains("RELEASE")){
                STAGES = STAGE + ",build_ami_prod,deploy_to_PROD"
            }else{
                throw new Exception("The TAG must include one of name \"RC\", \"SNAPSHOT\", \"RELEASE\" to deploy QA or PROD")
            }
        }catch(e){
            currentBuild.result = "FAILURE"
    //             throw(e)
            println(e)
        }finally{
            common_envs.add("APP_VERSION=${TAG}")
        }
    }
}

stage("Build and deploy AMI to QA"){
    // input(message: "Want to deploy AMI to QA?") 

    def selectedRegion = input(message: "Select region to deploy AMI", parameters: [
        extendedChoice(
           defaultValue: 'us-east-1,us-west-2',
           description: '',
           multiSelectDelimiter: ',',
           name: 'selectedRegion',
           quoteValue: false,
           saveJSONParameterToFile: false,
           type: 'PT_CHECKBOX',
           value:'us-east-1,us-west-2',
           visibleItemCount: 2
        )
    ])

    parallel (
        "QA-useast-1" : {
            node{
                stage("qa-us-east-1_build_ami"){
                    if(selectedRegion.contains("us-east-1")){
                        input(message: "Want to build AMI for QA us-east-1?")
                        node{
                            test_chef()
                            println "QA-us-east-1_build_ami"
                        }
                    }
                }
                stage("qa-us-east-1_deploy_ami") {
                    if(selectedRegion.contains("us-east-1")){
                        input(message: "Want to deploy AMI to QA us-east-1?")
                        node{
                            println "QA-us-east-1_deploy_ami"
                        }
                    }
                }
            }
        },
        "QA-uswest-2" : {
            stage("qa-us-west-2_build_ami"){
                if(selectedRegion.contains("us-west-2")){
                    input(message: "Want to build AMI for QA us-west-2?")
                    node{
                        println "QA-us-west-2_build_ami"
                    }
                }
            }
            stage("qa-us-west-2_deploy_ami") {
                if(selectedRegion.contains("us-west-2")){
                    input(message: "Want to deploy AMI to QA us-west-2?")
                    node{
                        println "QA-us-west-2_deploy_ami"
                    }
                }
            }
        }
    )
}

def apply_terraform(envList){

    withEnv(envList){
        sh '''#!/bin/bash
            set -x
            source $SCRIPTS_BASE/jenkins/assume_role.sh platform $envToDeploy-$regionToDeploy
            . /bin/virtualenvwrapper.sh
            workon platform

            touch changeAMI.sh
            chmod +x changeAMI.sh
            echo "#!/bin/bash" > changeAMI.sh
            echo "cd $TERRAFORM_HOME/scripts" >> changeAMI.sh
            echo "/bin/git pull" >> changeAMI.sh
            echo "sed -i \'s/\\bami-[^\\"]*/$AMI_ID/g\' $TERRAFORM_HOME/terraform/$envToDeploy/$regionToDeploy/_frontend_apps/$API/service/variables.tf" >> changeAMI.sh
            echo "sed -i \'s/\\bsun-ms-$API-[^\\"]*/sun-ms-$API-$APP_VERSION-$LT_NAME/g\' $TERRAFORM_HOME/terraform/$envToDeploy/$regionToDeploy/_frontend_apps/$API/service/variables.tf" >> changeAMI.sh

            ./changeAMI.sh

            rm -f changeAMI.sh
            cd $TERRAFORM_HOME/scripts

            ./terraform-runner.sh "terraform.plan(/$envToDeploy/$regionToDeploy/_frontend_apps/$API/service/)"

            sleep 20

            ./terraform-runner.sh "terraform.apply(/$envToDeploy/$regionToDeploy/_frontend_apps/$API/service/)"

            /bin/git add ../terraform/$envToDeploy/$regionToDeploy/_frontend_apps/$API/service/.log
            /bin/git add ../terraform/$envToDeploy/$regionToDeploy/_frontend_apps/$API/service/variables.tf

            /bin/git commit -m "updating $API to version $APP_VERSION"

            git push
        '''

    }
    
}

def test_chef(){
    CHEF_ENVIRONMENT = "qa-us-east-1-${SERVICE}"

    regex="^[qa|prod].*[1-2]\$"
    if((CHEF_ENVIRONMENT =~ regex).matches()){
        ATTRIBUTE="[\"platform-microservice\"][\"${SERVICE}\"][\"artifact\"][\"version\"]"
    }else{
        ATTRIBUTE="[\"b2c-microservice\"][\"${SERVICE}\"][\"artifact\"][\"version\"]"
    }

    common_envs.add("ATTRIBUTE=${ATTRIBUTE}")
    withEnv(common_envs){
        sh '''#!/bin/bash
            echo ".default_attribute$ATTRIBUTE == \"$APP_VERSION\""
        '''
    }
}

def apply_chef(envList, env, region) {

    CHEF_ENVIRONMENT = "${env}-${region}-${SERVICE}"
    regex="^[qa|prod].*[1-2]\$"
    if((CHEF_ENVIRONMENT =~ regex).matches()){
        ATTRIBUTE="[\"platform-microservice\"][\"${SERVICE}\"][\"artifact\"][\"version\"]"
    }else{
        ATTRIBUTE="[\"b2c-microservice\"][\"${SERVICE}\"][\"artifact\"][\"version\"]"
    }

    envList.add("ATTRIBUTE=${ATTRIBUTE}")
    withEnv(envList){
        sh '''#!/bin/bash
            set -x 
            source $SCRIPTS_BASE/jenkins/assume_role.sh platform $envToDeploy-$regionToDeploy
            eval "$(chef shell-init bash)"
            . /bin/virtualenvwrapper.sh
            workon platform

            $SCRIPTS_BASE/disable_chef.py -i ~/.ssh/id_rsa -e $CHEF_ENVIRONMENT -r $regionToDeploy -o sun-ms-$API -u ops-user -p --proxy ops-user@$BASTION

            # Update chef environment
            KNIFE_HOME=/var/lib/jenkins/.platform-chef
            ROLE=sun-ms-$API
            $SCRIPTS_BASE/chef/migrate_chef_environment -q ".default_attributes$ATTRIBUTE == \"$APP_VERSION\"" -k $KNIFE_HOME/knife.rb sun-ms-$API $CHEF_ENVIRONMENT sun-ms-${API}_VERSION="$APP_VERSION"

            RETVAL=$?
            (( ok = RETVAL & 1 ))
            (( changes = (RETVAL & 2) >> 1 ))
            GOOD=0
            YES=1

            set +x
        '''
    }
}

def queryASGInfo(envList){

    envList.add("ASG=sun-ms-${SERVICE}-asg")
    envList.add("autoscalingInfo=autoscalingInfo.out")
    // env.ASG="sun-ms-${API}-asg"
    // env.autoscalingInfo = "autoscalingInfo.out"

    withEnv(envList){
        sh '''#!/bin/bash
            set -x
            source $SCRIPTS_BASE/jenkins/assume_role.sh platform $envToDeploy-$regionToDeploy
            . /bin/virtualenvwrapper.sh
            workon platform

            aws autoscaling describe-auto-scaling-groups --auto-scaling-group-name $ASG | jq -r '.AutoScalingGroups[]' > $autoscalingInfo 

            jq '. | .MinSize,.MaxSize,.MixedInstancesPolicy.InstancesDistribution.OnDemandBaseCapacity,.MixedInstancesPolicy.InstancesDistribution.OnDemandBaseCapacity' $autoscalingInfo > asginfo.out
            set +x
        '''
    }

    ASGINFO = readFile('asginfo.out')
    return ASGINFO
}

def rolling_upgrade(envList){

    def launchInstance = "launchConfiguration"
    def OPTIONARGS = ""
    def MINSIZE = 0
    def MAXSIZE = 0
    def ONDEMANDCAPACITY = 0
    def ONDEMANDRATIO = 0
    def CALCULATEDMINSIZE = 0
    def CALCULATEDMAXSIZE = 0
    def CALCULATEDONDEMANDRATIO = 0

    ASGINFO = queryASGInfo(envList)
    def list = ASGINFO.readLines()

    println list.size()

    MINSIZE = list[0].trim().toInteger()
    MAXSIZE = list[1].trim().toInteger()
    if(list[2] == "null"){
        launchInstance = "launchTemplate"
    }else{
        ONDEMANDCAPACITY = list[2].trim().toInteger()
        ONDEMANDRATIO = list[3].trim().toInteger()
    }
    
    CALCULATEDMINSIZE = MINSIZE * 2
    CALCULATEDMAXSIZE = MAXSIZE
    CALCULATEDONDEMANDRATIO = 0

    if (launchInstance == "launchTemplate" && ONDEMANDCAPACITY != 0){
        CALCULATEDMINSIZE = ((int)((MINSIZE * 2) * ((ONDEMANDCAPACITY/MINSIZE) + 1) + 1) * 100)/100
        CALCULATEDONDEMANDRATIO = (int)((ONDEMANDCAPACITY/MINSIZE) *100)
        OPTIONARGS = " --max-size \$CALCULATEDMAXSIZE --mixed-instances-policy \'{\"InstancesDistribution\":{\"OnDemandPercentageAboveBaseCapacity\":\$CALCULATEDONDEMANDRATIO}}\'"
    }

    if(CALCULATEDMINSIZE > MAXSIZE){
        CALCULATEDMAXSIZE = CALCULATEDMINSIZE + MAXSIZE
    }

    println "${MAXSIZE} - ${MINSIZE} - ${ONDEMANDCAPACITY} - ${ONDEMANDRATIO} - ${CALCULATEDONDEMANDRATIO}"

    println "before update asg"

    envList.add("CALCULATEDMINSIZE=${CALCULATEDMINSIZE}")
    envList.add("CALCULATEDMAXSIZE=${CALCULATEDMAXSIZE}")
    envList.add("CALCULATEDONDEMANDRATIO=${CALCULATEDONDEMANDRATIO}")
    envList.add("OPTIONARGS=${OPTIONARGS}")
    envList.add("MIN=${MINSIZE}")

    withEnv(envList){
        sh '''#!/bin/bash
            set -x
            source $SCRIPTS_BASE/jenkins/assume_role.sh platform $envToDeploy-$regionToDeploy
            . /bin/virtualenvwrapper.sh
            workon platform

            echo Updating Autoscaling Group $ASG
            aws autoscaling update-auto-scaling-group --auto-scaling-group-name $ASG --min-size $CALCULATEDMINSIZE --desired-capacity $CALCULATEDMINSIZE $OPTIONARGS

            echo Waiting for Autoscaling Group to spin up new nodes

            while [[ $SPINUPPENDING != "0" ]]; do
                ASGINSERVICE=`aws autoscaling describe-auto-scaling-groups --auto-scaling-group-name $ASG | jq -r '.AutoScalingGroups[].Instances[].InstanceId' | wc -l | tr -d ' '`
                SPINUPPENDING=$(($CALCULATEDMINSIZE - $ASGINSERVICE))
                echo waiting on $SPINUPPENDING nodes to be created.
                sleep 20
            done

            echo Waiting for nodes to become healthy in Autoscaling Group

            while [[ $ASGPENDING != "0" ]]; do
                ASGTOTALNODES=`aws autoscaling describe-auto-scaling-groups --auto-scaling-group-names $ASG | jq -r '.AutoScalingGroups[].Instances[].HealthStatus' | wc -l | tr -d ' '`
                ASGHEALTHY=`aws autoscaling describe-auto-scaling-groups --auto-scaling-group-names $ASG | jq -r '.AutoScalingGroups[].Instances[].HealthStatus' | grep Healthy | wc -l | tr -d ' '`
                ASGPENDING=$(($ASGTOTALNODES - $ASGHEALTHY))
                echo $ASGPENDING not in service
                sleep 10
            done

            ### Adding a sleep for a minute ###
            echo "sleeping for one minutes"
            sleep 60

            ### Wait for nodes to become healthly in the ELB
            echo waiting for nodes to become healthy in the ELB
            ELB=sun-ms-$API-$envToDeploy-lb

            while [[ $PENDING != "0" ]]; do
                INSERVICE=`aws elb describe-instance-health --load-balancer-name $ELB  | jq -r '.InstanceStates[].State'  | grep InService | wc -l | tr -d ' '`
                PENDING=$(( $CALCULATEDMINSIZE - $INSERVICE))
                echo $PENDING not in service
                sleep 30
            done

            echo "sleeping for one more minutes to allow for any slow chef runs"
            sleep 60

            ### Scale down

            echo Scaling down the Autoscaling Group $ASG
            aws autoscaling update-auto-scaling-group --auto-scaling-group-name $ASG --min-size $MIN --desired-capacity $MIN

            while [[ $ELBPENDING != $MIN ]]; do
                ELBPENDING=`aws elb describe-instance-health --load-balancer-name $ELB  | jq -r '.InstanceStates[].State'  | grep InService | wc -l | tr -d ' '`
                echo $ELBPENDING node in service, scaling down to $MIN
                sleep 20
            done
            set +x
        '''
    }
}

def find_AMI_VERSION(envToBuildAMI, regionToBuildAMI){

    version=0
    exist = true

    while(exist){
        APPLICATION_ARTIFACT= "sun-ms-${SERVICE}-${TAG}-${version}"
        exist = check_exist_AMI(APPLICATION_ARTIFACT, envToBuildAMI, regionToBuildAMI)

        if(exist){
            version++
        }
    }

    return version
}

def check_exist_AMI(String APPLICATION_ARTIFACT_NAME, String envToBuildAMI, String regionToBuildAMI) {

    exist = false

    withEnv([
        "existAMIOutputFile=${envToBuildAMI}_${regionToBuildAMI}_AMI.out",
        "APPLICATION_ARTIFACT=${APPLICATION_ARTIFACT_NAME}",
        "envToBuildAMI=${envToBuildAMI}",
        "regionToBuildAMI=${regionToBuildAMI}",
        "AWS_DEFAULT_REGION=${regionToBuildAMI}",
        "AWS_REGION=${regionToBuildAMI}"
    ]){
        sh '''#!/bin/bash
            source $SCRIPTS_BASE/jenkins/assume_role.sh platform $envToBuildAMI-$regionToBuildAMI
            aws ec2 describe-images --filter Name=tag-value,Values=$APPLICATION_ARTIFACT | jq -r '.Images[].ImageId' > $existAMIOutputFile
        '''

    }

    if (readFile("${envToBuildAMI}_${regionToBuildAMI}_AMI.out").trim() != "" ){
        exist = true
    }

    return exist
}

def build_AMI(envList) {

    withEnv(envList){
        sh '''#!/bin/bash
            source $SCRIPTS_BASE/jenkins/assume_role.sh platform $envToBuildAMI-$regionToBuildAMI
            . /bin/virtualenvwrapper.sh
            workon platform

            /usr/local/bin/packer build --var-file=$SCRIPTS_BASE/jenkins/packer-terraform/sun-ms/$envToBuildAMI-$regionToBuildAMI.json -var api=$API -var app_version=$APP_VERSION -var ami_version=$AMI_VERSION -var ami_prefix=sun-ms-$API -var assembly=$ASSEMBLY -var artifact_prefix=$artifact_prefix $SCRIPTS_BASE/jenkins/packer-terraform/sun-ms/packer.json
        '''
    }
}
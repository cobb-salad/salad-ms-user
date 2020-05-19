AMI_VERSION = 0
stage('Parameter Check'){
    node{
        TAG = "${params.TAG}"
        BUILD_TYPE = "${params.BUILD_TYPE}"
        SCALA_VERSION = "${params.SCALA_VERSION}"
        ASSEMBLY = "${params.ASSEMBLY}"
        SERVICE = "${params.SERVICE}"
        AMI_VERSION = "${params.AMI_VERSION}"
        AUTO_INCREMENT_AMI_VERSION = "${params.AUTO_INCREMENT_AMI_VERSION}"
        RUN_TASK = "${params.RUN_TASK}"

        println "${BRANCH_NAME}"
        println "${TAG}"
        println "${BUILD_TYPE}"
        println "${SCALA_VERSION}"
        println "${ASSEMBLY}"
        println "${SERVICE}"
        println "${AUTO_INCREMENT_AMI_VERSION}"
        println "${RUN_TASK}"

        try{
            if (params.SERVICE == "") {
                throw new Exception("You must select Service")
            }
            if (params.TAG == "") {
                throw new Exception("Enter the artifact version for the build in TAG")
            }
        }catch(e){
            currentBuild.result = "FAILURE"
    //             throw(e)
            println(e)
        }
    }

}

stage("Git CheckOut"){
    node{
        println "Git CheckOut Started"
        checkout(
                [
                        $class                           : 'GitSCM',
                        branches                         : [[name: 'master']],
                        doGenerateSubmoduleConfigurations: false,
                        extensions                       : [],
                        submoduleCfg                     : [],
                        userRemoteConfigs                : [[credentialsId: 'github_mjkong_ssh', url: 'https://github.com/cobb-salad/salad-ms-user.git']]
                ]
        )
        println "Git CheckOut End"
    }
}

stage("Build Artifact") {
    node{
        if (params.BUILD_TYPE == "SBT"){

            BUILD_DIR="target/scala-${SCALA_VERSION}"
            SBT_LAUNCHER="1.5"
            JVM_OPTION="-Djavax.net.ssl.trustStore=/etc/pki/ca-trust/extracted/java/cacerts"
            SBT_OPTION="-Dsbt.log.noformat=true"
            SBT_ACTION="clean assembly"

            sh "ls -la"
        }else{
            BUILD_DIR="build/libs"
            GRADLE_TASK="clean install"
        }
    }
}

stage("Build AMI") {
    node{

        if(AMI_VERSION > 0){
            println "AMI_VERSION is greater than 0"
        }

        env.AMI_VERSION="${AMI_VERSION}"

    //         userInput = input(
    //             id: 'Proceed1', message: 'AMI_VERSION is ${env.AMI_VERSION} : Do you want to use this version?', parameters: [
    //             [$class: 'BooleanParameterDefinition', defaultValue: true, description: '', name: 'Please confirm you agree with this']
    //          ])
    //
    //         println "${userInput}"
    }
}

stage("after build ami"){
    node{
        env.TESTVAL2="ttt"
        env.AMI_ID="ami-123456789"
        env.ENVIRONMENT="qa"
        ENVIRON = "${env.ENVIRONMENT}"

        sh """#!/bin/bash
            set -x
            touch test2
            echo '\"ami-test\"' > test2

            touch change.sh
            chmod +x change.sh
            ls -al

            echo "#!/bin/bash" > change.sh
            cat change.sh
            #echo "sed -i \'s/ami-[^\"]*/ami-123456789/g\' test2" >> change.sh
            echo "sed -i \'s/\\bami-[^\\"]*/$AMI_ID/g\' test2" >> change.sh
            #sed -i 's/[^\"]*/'"$TESTVAL2"'/g' test2
            ./change.sh
            set +x
        """
        test()
    }
}

def getASGINFO(jsonString){

    env.ASGINFO = "/var/lib/jenkins/jsontmp"

    sh '''#!/bin/bash
        jq '. | .MinSize,.MaxSize,.MixedInstancesPolicy.InstancesDistribution.OnDemandBaseCapacity,.MixedInstancesPolicy.InstancesDistribution.OnDemandBaseCapacity' /var/lib/jenkins/jsontmp > asginfo.out
    '''

    ASGOUT = readFile("asginfo.out")
    def list = ASGOUT.readLines()

    return ASGOUT
}

def jsontest(){

    env.ASGINFO = "/var/lib/jenkins/jsontmp"
    ASGINFO=readFile("/var/lib/jenkins/jsontmp")
//     def object= new JsonSlurper().parseText(ASGINFO)
    def ASGINFOObj = jsonSlurper.parseText(ASGINFO)

    MINSIZE = ASGINFOObj.get('MinSize')
    MAXSIZE = ASGINFOObj.get('MaxSize')
    ONDEMANDCAPACITY = ASGINFOObj.get('MixedInstancesPolicy').get('InstancesDistribution').get('OnDemandBaseCapacity')
    ONDEMANDRATIO = ASGINFOObj.get('MixedInstancesPolicy').get('InstancesDistribution').get('OnDemandPercentageAboveBaseCapacity')

//     MINSIZE = ASGINFOObj.MinSize
//     MAXSIZE = ASGINFOObj.MaxSize
//     ONDEMANDCAPACITY = ASGINFOObj.MixedInstancesPolicy.InstancesDistribution.OnDemandBaseCapacity
//     ONDEMANDRATIO = ASGINFOObj.MixedInstancesPolicy.InstancesDistribution.OnDemandPercentageAboveBaseCapacity

    ONDEMANDCAPACITY = 3
    ONDEMANDRATIO = 0
    OPTIONARGS = ""

    CALCULATEDMINSIZE = (MINSIZE * 2) * ((ONDEMANDCAPACITY / MINSIZE) + 1) + 1
    CALCULATEDMINSIZE = ((int) CALCULATEDMINSIZE * 100)/100
    CALCULATEDONDEMANDRATIO = (int)((ONDEMANDCAPACITY/MINSIZE) *100)

    env.CALCULATEDONDEMANDRATIO = "${CALCULATEDONDEMANDRATIO}"

    println "${CALCULATEDONDEMANDRATIO}"

    env.OPTIONARGS = "${OPTIONARGS}"


    sh '''#!/bin/bash
        set -x

        echo test $CALCULATEDONDEMANDRATIO

        echo $CALCULATEDONDEMANDRATIO
        echo $OPTIONARGS

        set +x
    '''
}

def test(){

    // ASGOUT=getASGINFO("t")
    // def list = ASGOUT.readLines()

    MIN=sh(returnStdout: true, script: 'echo test').trim()
    println "${MIN}"

}

def returnTest(){
    return readFile("/var/lib/jenkins/jsontmp")
}


def apply_chef(){

    sh '''#!/bin/bash
        set -x
        ./sun-ms-api-${ENVIRONMENT}_version = \"test\"
        set +x
    '''
}
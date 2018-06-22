#!/usr/bin/env python3

import os, sys, glob
from tempfile import mkdtemp
from shutil import rmtree
from typing import List, Optional

ORG_NAME = 'stevetarver'
TEMPLATE_REPO = 'ms-ref-templates'
LOCAL_TEST = True


class Term:
    """
    See https://stackoverflow.com/questions/287871/print-in-terminal-with-colors/21786287#21786287
    for color codes
    """
    _prefix = '===>'
    _yellow = '\x1b[1;33;40m'
    _red = '\x1b[1;31;40m'
    _end = '\x1b[0m'

    @staticmethod
    def input(msg: str) -> str:
        return input(f"{Term._prefix} {msg}: ")

    @staticmethod
    def info(msg: str) -> None:
        print(f"{Term._prefix} {msg}")

    @staticmethod
    def warn(msg: str) -> None:
        print(f"{Term._yellow}{Term._prefix} {msg}{Term._end}")

    @staticmethod
    def error(msg: str) -> None:
        print(f"{Term._red}{Term._prefix} {msg}{Term._end}")


class Config:
    """ User input for template replacement params """

    def __init__(self):
        # Kind of project to build, identified by this repo's directory names
        self._template = ''

        # The production DNS Top Level Domain plus the domain name. E.g. 'makara.com'
        self.prod_dns_domain = ''
        # The production DNS Host portion of the DNS name; everything that precedes dns_domain needed
        # to reach the API ingress endpoint. E.g. if 'api'. The DNS host + domain yield 'api.makara.com'.
        self.prod_dns_host = ''
        # An internal TLD and domain name that are the suffix for the ops endpoint. E.g. 'makara.dom'.
        # The GitHub repo name is prefixed to form an ops endpoint providing greater access than the
        # production endpoint; for example, access to health and metrics.
        self.ops_dns_domain = ''
        # The business functional or logical domain. E.g. 'finance', 'hr', 'network'.
        self.bus_domain = ''
        # A further clarification of the business domain to provide granularity to reduce growing
        # service counts in any one business domain. E.g. The 'finance' domain could have 'payroll'
        # and 'reporting' sub-domains.
        self.bus_subdomain = ''

        # Path to temp dir holding our cloned repo (our template source)
        self.template_source_dir = ''
        # Path to the new project
        self.template_target_dir = os.getcwd()
        # Name of the target github repo
        self.github_repo_name = os.path.basename(self.template_target_dir)

        if not os.path.exists('.git'):
            Messages.target_dir_not_git_initialized()
            exit(1)
        if len(os.listdir('.')) > 1:
            Messages.target_dir_not_empty()
            exit(1)

    def __str__(self) -> str:
        return f"""
    template:            {self._template}
    language:            {self.language()}

    template_target_dir: {self.template_target_dir}
    github_repo_name:    {self.github_repo_name}
    ops_dns_domain:      {self.ops_dns_domain}
    ops_dns_name:        {self.ops_dns_name()}

    prod_dns_domain:     {self.prod_dns_domain}
    prod_dns_host:       {self.prod_dns_host}
    prod_dns_name:       {self.prod_dns_name()}
    bus_domain:          {self.bus_domain}
    bus_subdomain:       {self.bus_subdomain}
    root_package:        {self.root_package()}
    uri:                 {self.uri()}

    template_source_dir: {self.template_source_dir}
    """

    def is_valid(self) -> bool:
        """ Do all required fields exist and pass basic validation testing """
        values = (self._template,
                  self.prod_dns_domain,
                  self.prod_dns_host,
                  self.ops_dns_domain,
                  self.bus_domain,
                  self.bus_subdomain)
        if None in values:
            return False
        if '' in values:
            return False
        # Other checks
        # - _template is a directory in this repo
        # - dns_domains have dots
        return True

    def language(self) -> str:
        if 'java-spring-maven' == self._template:
            return 'java'
        elif 'groovy-spring-gradle' == self._template:
            return 'groovy'
        else:
            raise Exception("Unknown language")

    def mvn_group(self) -> str:
        """ Reversed prod_dns_name + bus_domain. e.g. com.makara.finance """
        return '.'.join(('.'.join(reversed(self.prod_dns_domain.split('.'))),
                         self.bus_domain))

    def ops_dns_name(self) -> str:
        """ e.g. fin-payroll.makara.dom """
        return f"{self.github_repo_name}.{self.ops_dns_domain}"

    def root_package(self) -> str:
        """ Reversed prod_dns_name + bus_domain + bus_subdomain. e.g. com.makara.finance.payroll """
        return '.'.join((self.mvn_group(), self.bus_subdomain))

    def root_package_path(self) -> str:
        """ e.g. com/makara/finance/payroll """
        return self.root_package().replace('.', '/')

    def print(self) -> None:
        Term.info("Using this configuration:")
        print(f"     Target dir       : {self.template_target_dir}")
        print(f"     GitHub repo name : {self.github_repo_name}")
        print(f"     Template         : {self._template}")
        print(f"     Prod DNS name    : {self.prod_dns_name()}")
        print(f"     Ops DNS name     : {self.ops_dns_name()}")
        print(f"     Root URI         : {self.uri()}")
        print(f"     Example prod URL : https://{self.prod_dns_name()}{self.uri()}")
        print(f"     Example ops URL  : https://{self.ops_dns_name()}{self.uri()}")
        print(f"     Package          : {self.root_package()}")
        if 'maven' in self._template:
            print(f"     Maven group      : {self.mvn_group()}")

    def prod_dns_name(self) -> str:
        """ e.g. api.makara.com """
        return f"{self.prod_dns_host}.{self.prod_dns_domain}"

    def template(self) -> str:
        """ One of the template directories in this repo. e.g. groovy-spring-gradle """
        return self._template

    def uri(self):
        """ /bus_domain/bus_subdomain. e.g. /finance/payroll """
        return f"/{self.bus_domain}/{self.bus_subdomain}"

    def use_groovy_template(self) -> None:
        self._template = 'groovy-spring-gradle'

    def use_java_template(self) -> None:
        self._template = 'java-spring-maven'


class Messages:
    """ Simple messages printing to users """

    @staticmethod
    def instructions() -> None:
        print('''Service Generator

Initialize a new git repo with skeleton service code.

The following must be true:
 1. I have created an empty git repository 
 2. I have no other files in that directory (only the .git directory)
 3. I started this script from that new repo directory

Now, we will collect some information to customize your project
 Template:           Which template to use - one of: java-spring-maven, groovy-spring-gradle
 Prod DNS domain:    The production DNS Top Level Domain plus the domain name. E.g. 'makara.com'
 Prod DNS host:      The production DNS Host portion of the DNS name; everything that precedes 
                     the dns_domain needed to reach the API ingress endpoint. E.g. 'api'. 
                     The DNS host + domain yield 'api.makara.com'.
 Ops DNS domain:     An internal TLD and domain name that are the suffix for the ops endpoint. 
                     E.g. 'makara.dom'. The GitHub repo name is prefixed to form an ops endpoint 
                     providing greater access than the production endpoint; for example, access 
                     to health and metrics.
 Business domain:    The domain is a family of services that this service belongs to; the 
                     functional area it will target. E.g. billing, network, etc.
                     The domain is used to define the java package and ReST paths.
 Business subdomain: The subdomain refines the scope of the service. Smaller domains
                     may use the main, top level resource collections. 
                     E.g. quotes in /billing/quotes, vpn in /network/vpn
                     The subdomain is used to define the ReST URI (path).

If you don't like the resulting package structure, you can:
 1. Delete all files in this directory, git init, start over.
 2. Refactor the package paths in your IDE and manually update the path in the
    second host rule in helm/<github_repo_name>/templates/ingress.yaml
''')

    @staticmethod
    def remaining_tasks(config: Config) -> None:
        Term.info(f"""Next Steps:
     1. Review code and refine naming as necessary
     2. Run project and test endpoints
        * 'mvn spring-boot:run' OR './gradlew bootRun'
        * http://localhost:8080/actuator
        * http://localhost:8080/prometheus
        * http://localhost:8080/healthz/readiness
        * http://localhost:8080/healthz/liveness
        * http://localhost:8080/healthz/ping
        * http://localhost:8080{config.uri()}/contacts
     3. Push to GitHub
     4. Create an appropriate DNS name as ops endpoint, pointing to k8s ingress nodes
     5. Create a Jenkins build job
        * Multibranch 
        * Point it at this GitHub repo
     6. Build
     7. Verify k8s deploy: http://{config.ops_dns_name()}{config.uri()}/contacts
     8. Visit TODO tags for advanced configuration""")

    @staticmethod
    def target_dir_not_git_initialized() -> None:
        Term.error('''The current directory does not look like a GitHub repo dir - no .git directory present.
     Please review the above instructions to help identify the problem.
     Perhaps you are not in the GitHub repo dir. If not, cwd to the GitHub repo dir and retry.
     Cowardly refusing to continue.''')

    @staticmethod
    def target_dir_not_empty() -> None:
        Term.error('''The current directory has files in it (other than the .git dir).
     Please review the above instructions to help identify the problem.
     Perhaps you are not in the GitHub repo dir. If so, remove those files and retry.
     Cowardly refusing to continue.''')


def cleanup(config) -> None:
    """ Remove temporary items """
    if not LOCAL_TEST:
        Term.info(f"Removing {config.template_source_dir}")
        #rmtree(config.template_source_dir)


def clone_template(config: Config) -> None:
    """
    Clone TEMPLATE_REPO to a temp dir

    Also
    - set config.template_source_dir - location of cloned repo
    """
    if not LOCAL_TEST:
        config.template_source_dir = mkdtemp(prefix=f"{TEMPLATE_REPO}")
        Term.info(f"Created temp directory {config.template_source_dir}")

        Term.info(f"Cloning {TEMPLATE_REPO} to temp dir")
        cwd = os.getcwd()
        os.chdir(config.template_source_dir)
        os.system(f"git clone https://github.com/{ORG_NAME}/{TEMPLATE_REPO}.git")
        os.chdir(cwd)
    else:
        # Bypass GitHub download when local testing. Assumes:
        # - you called this script from the target dir
        # - location of this script identifies the repo dir, its parent is the source dir
        config.template_source_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))


def commit_changes(config: Config) -> None:
    """ Commit all changes to git repo """
    Term.info("Committing project files to git")
    os.system('git add --all')
    os.system(f"git commit -am 'Initial {config.template()} template'")


def copy_template(config) -> None:
    """ Copy the template source to the target dir """
    Term.info(f"Copying template to new GitHub repo {config.template_target_dir}")
    template_dir = f"{config.template_source_dir}/{TEMPLATE_REPO}/{config.template()}/"
    os.system(f"cp -r {template_dir} {config.template_target_dir}")


def gather_user_input() -> Config:
    """ Get required input from user """
    config = Config()
    Messages.instructions()

    while '' == config.template():
        template = Term.input("Which template? [j]ava or [g]roovy")
        if template in ['j', 'J']:
            config.use_java_template()
        elif template in ['g', 'G']:
            config.use_groovy_template()
        else:
            Term.error(f"Unknown template type '{template}'")

    config.prod_dns_domain = Term.input("Enter your Prod DNS domain")
    config.prod_dns_host = Term.input("Enter your Prod DNS host")
    config.ops_dns_domain = Term.input("Enter your Ops DNS domain")
    config.bus_domain = Term.input("Enter your Business domain")
    config.bus_subdomain = Term.input("Enter your Business subdomain")

    config.print()
    ok = Term.input("Continue? [Yn]")
    if ok not in ['y', 'Y', '']:
        Term.info("Exiting at your request")
        exit(1)

    return config


def get_file_list(root_dir: str) -> List[str]:
    """ Return a list of paths to all files in all directories and sub-directories """
    result = []
    for filename in glob.iglob(root_dir + '**/*', recursive=True):
        if os.path.isfile(filename) and not filename.endswith('.jar'):
            result.append(filename[2:])
    return result


def initialize() -> Optional[Config]:
    """
    Allow users to specify options on command line.

    All options must be specified.
    Options must be specified as --member=value
    """
    if len(sys.argv) > 1:
        # all args must start with --
        args = sys.argv[1:]
        if len(args) != 6:
            Term.error(f"All args must be specified. You provided the wrong arg count: '{args}'")
            exit(1)
        s = set([x[:2] for x in args])
        if len(s) != 1 or '--' not in s:
            Term.error(f"All arg keys must have a '--' prefix: --bus_domain=finance. You provided '{args}'")
            exit(1)
        # all args must be kv pairs delimited by =
        s = set('=' in x for x in args)
        if len(s) != 1 or True not in s:
            Term.error(f"All args must be '=' delimited kv pairs: --bus_domain=finance. You provided '{args}'")
            exit(1)

        config = Config()
        args = dict((x[2:].split('=') for x in args))

        config._template = args['template']
        config.prod_dns_domain = args['prod_dns_domain']
        config.prod_dns_host = args['prod_dns_host']
        config.ops_dns_domain = args['ops_dns_domain']
        config.bus_domain = args['bus_domain']
        config.bus_subdomain = args['bus_subdomain']
        if config.is_valid():
            return config
    return None


def replace_keywords(config: Config) -> None:
    """ Customize the template using user variables """
    params = {
        '~~GITHUB_REPO_NAME~~': config.github_repo_name,
        '~~ROOT_PACKAGE~~': config.root_package(),
        '~~MVN_GROUP~~': config.mvn_group(),
        '~~ROOT_URI~~': config.uri().replace('/', '\\/'),
        '~~BUS_DOMAIN~~': config.bus_domain,
    }
    files = get_file_list('./')

    Term.info(f"Applying template to {config.template_target_dir}")
    for k,v in params.items():
        for file in files:
            os.system(f"sed -i '' 's/{k}/{v}/g' {file} > /dev/null")


def update_directories(config: Config) -> None:
    """ Move template directories to user specified package path """
    Term.info("Updating directory structure")

    lang = config.language()
    target_path = config.root_package_path()

    for item in ['main', 'test']:
        base = f"src/{item}/{lang}"
        os.makedirs(f"{base}/{target_path}/")
        code = os.system(f"mv {base}/package/* {base}/{target_path}/")
        if 0 != code:
            Term.error(f"Problem moving package - see above error. Cannot continue.")
            exit(code)
        os.rmdir(f"{base}/package/")

    # Rename helm/main to helm/~~GITHUB_REPO_NAME~~
    os.system("mv helm/main helm/{}".format(config.github_repo_name))


def main():

    # When executing a script by piping a curl'd file to python3, stdin is opened as a FIFO.
    # We need to reopen stdin as a tty to accept user input
    sys.stdin = open("/dev/tty")

    config = initialize() or gather_user_input()
    if LOCAL_TEST:
        Term.info(f"Using this config: {config}")

    clone_template(config)
    copy_template(config)
    replace_keywords(config)
    update_directories(config)
    commit_changes(config)

    cleanup(config)
    Messages.remaining_tasks(config)

    # TODO: Automate validation testing: start the project & curl the endpoints


# We may only be called as a script
main()

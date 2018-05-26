# You'll need

- AtlassianSDK [Follow this link for installation](https://developer.atlassian.com/display/DOCS/Introduction+to+the+Atlassian+Plugin+SDK)
- npm v5.6.0+

# Prepare env vars

- Make your own `config.json` based on the `config.json.example` file

You can use whatever you want for `jwt`.
The `api_url` will be the ngrok url that you'll be using for the bot [follow these steps for installation](https://bitbucket.org/nearsoftlabs/standup-bot/src/development/)

# How to build the app

If you go through the code, you'll notice some errors related with a missing Java class.

Don't worry about that for now. That missing class will be genearted in a grunt task.

Depending on your target environment you have to use a specific grunt task:

**local: ** `grunt`
**stage: ** `grunt stage`
**production: ** `grunt production`

# How to run

- `atlas-run`

# How to update on change

- `npm run build`

# How to debug

Instead of start Jira with `atlas-run`, use `atlas-debug`

# How to run tests

- `atlas-unit-test`

# Here are the SDK commands you'll use immediately:

* atlas-run   -- installs this plugin into the product and starts it on localhost
* atlas-debug -- same as atlas-run, but allows a debugger to attach at port 5005
* atlas-cli   -- after atlas-run or atlas-debug, opens a Maven command line window:
                 - 'pi' reinstalls the plugin into the running product instance
* atlas-help  -- prints description for all commands in the SDK

# Full documentation is always available at:

https://developer.atlassian.com/display/DOCS/Introduction+to+the+Atlassian+Plugin+SDK

# Question?

- You can always ask the Labs team (specifically for this project, you can ask Frank)

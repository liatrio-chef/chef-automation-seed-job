Intro
=====

A Jenkins DSL job to create Chef CI jobs for:
1. unit tests
2. test kitchen
3. increment cookbook version and update master repo if all tests pass

Currently hardcoded all github.com/liatrio-chef/*-liatrio cookbooks for now.

Usage
-----
In Jenkins, create a new freestyle project. Add a 'Process DSL Job' and reference the chef_automation_seed_job.groovy file.

Contributing
------------
1. Fork the repository on Github
2. Create a named feature branch (like `add_component_x`)
3. Write your change
4. Write tests for your change (if applicable)
5. Run the tests, ensuring they all pass
6. Submit a Pull Request using Github

License and Authors
-------------------
Authors: Drew Holt <drew@liatrio.com>
test

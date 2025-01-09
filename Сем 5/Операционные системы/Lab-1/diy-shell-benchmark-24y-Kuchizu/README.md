# C++ Monolith App Template

## Getting Started

1. Open the project in the VSCode.

2. Click "Reopen in Container" in a VSCode notification.

3. Do `bash ci/prepare.bash`.

4. Do `bash ci/precommit.bash`.

5. Replace `monolith` and `MONOLITH` with your project name.

6. Repeat steps 1-4.

## Notes

- Application executable is available at `{build_dir}/source/{project_name}`.

- To enable `benchmark` module configure the project with `{project_name}_BENCHMARK=ON`.

- Benchmark executables are available at `{build_dir}/benchmark/{project_name}-bench-{bench_name}`.

- Do not forget to use `Asan` build mode for debugging.

- Press F5 to build and run tests under a debuger in VSCode UI.

## Thanks

- <https://gitlab.com/Lipovsky/twist>

- <https://github.com/vityaman-edu/bst>

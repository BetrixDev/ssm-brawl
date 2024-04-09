import type { Proc, StartOptions, ProcessDescription } from "pm2";
import pm2 from "pm2";

export async function spawnProcess(opts: StartOptions) {
  return new Promise<Proc>((res, rej) => {
    pm2.start(opts, (err, proc) => {
      if (err instanceof Error) {
        rej(err);
      }

      res(proc);
    });
  });
}

export async function deleteProcessesByName(name: string) {
  return new Promise<void>((res, rej) => {
    pm2.delete(name, (err) => {
      if (err instanceof Error) {
        return rej(err);
      }

      res();
    });
  });
}

export async function listRunningProcesses() {
  return new Promise<ProcessDescription[]>((res, rej) => {
    pm2.list((err, list) => {
      if (err instanceof Error) {
        return rej(err);
      }

      res(list);
    });
  });
}

export async function getProcessesByName(name: string) {
  const allProcesses = await listRunningProcesses();
  return allProcesses.filter((p) => p.name === name);
}

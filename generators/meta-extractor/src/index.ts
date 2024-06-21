import path from "path";
import { glob } from "glob";
import { readFileSync, writeFileSync } from "fs";
import z from "zod";
import Case from "case";

const META_VALUE_ASSIGNMENT_PATTERN =
  /(private)\s+(val)\s+(?<variableName>\w+)\s*=\s*(?<metaMethod>\w+)\("(?<metaName>[^"]+)"(?:,\s*(?<defaultValue>\d+))?\)/;

const MetaValueGroup = z.object({
  variableName: z.string(),
  metaMethod: z.string(),
  metaName: z.string(),
  defaultValue: z.string(),
});

type MetaValue = {
  dataType: string;
  defaultValue: string;
  inlineName: string;
  dbName: string;
  displayName: string;
};

const PLUGIN_BASE_DIR = path.join(process.cwd(), "..", "..", "plugin");

const sourceFiles = (
  await glob("**/{passives,abilities}/[!I]*.kt", {
    cwd: PLUGIN_BASE_DIR,
  })
).filter((src) => !src.includes("utils"));

const output: any[] = [];

sourceFiles.forEach((sourcePath) => {
  const sourceFullPath = path.join(PLUGIN_BASE_DIR, sourcePath);
  const contents = readFileSync(sourceFullPath).toString();
  const lines = contents.split("\n").map((line) => line.trim());

  const metaValues: MetaValue[] = [];

  lines.forEach((line) => {
    const matchResult = line.match(META_VALUE_ASSIGNMENT_PATTERN);

    if (matchResult) {
      const { groups } = matchResult;
      const parsedGroups = MetaValueGroup.safeParse(groups);

      if (!parsedGroups.success) {
        return;
      }

      const varName = parsedGroups.data.variableName;

      const dataType = parsedGroups.data.metaMethod.includes("Int")
        ? "int"
        : varName.includes("Double")
          ? "double"
          : "string";

      metaValues.push({
        dataType,
        inlineName: parsedGroups.data.variableName,
        dbName: parsedGroups.data.metaName,
        defaultValue: parsedGroups.data.defaultValue,
        displayName: Case.title(parsedGroups.data.metaName),
      });
    }
  });

  if (metaValues.length === 0) {
    return;
  }

  const fileName = path.basename(sourceFullPath).split(".")[0];

  output.push({
    type: fileName.includes("Ability") ? "ability" : "passive",
    id: Case.camel(fileName.replace(/(Passive|Ability)/, "")),
    values: metaValues,
  });
});

console.log(JSON.stringify(output, null, 2));

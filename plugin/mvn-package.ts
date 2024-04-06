import maven from "maven";

const mvn = maven.create({
  cwd: ".",
});

async function main() {
  console.log("Building plugin...");
  await mvn.execute(["package"]);
}

main();

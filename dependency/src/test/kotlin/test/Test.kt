package test//package test
//
//import net.minestom.dependencies.DependencyGetter
//import net.minestom.dependencies.DependencyResolver
//import net.minestom.dependencies.ResolvedDependency
//import net.minestom.dependencies.UnresolvedDependencyException
//import net.minestom.dependencies.maven.MavenRepository
//import org.junit.jupiter.api.Test
//import java.nio.file.Path
//
//class Test {
//    @Test
//    fun test() {
//        class MyResolver: DependencyResolver {
//            override fun resolve(id: String, targetFolder: Path): ResolvedDependency {
//                throw UnresolvedDependencyException(id)
//            }
//        }
//        val dependencyGetter = DependencyGetter()
//            .addResolver(MyResolver())
//            .addMavenResolver(repositories = listOf(
//                MavenRepository.Jitpack,
//                MavenRepository.Central,
//                MavenRepository.JCenter,
//                MavenRepository("Minecraft Libs", "https://libraries.minecraft.net"),
//                MavenRepository("Sponge", "https://repo.spongepowered.org/maven"),
//            ))
//        val resolved = dependencyGetter.get("dev.hollowcube:polar:1.12.1", Path.of("libs"))
//        val resolved2 = dependencyGetter.get("dev.hollowcube:polar:1.12.2", Path.of("libs"))
//        resolved.printTree()
//    }
//}
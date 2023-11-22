import { Routes } from '@angular/router'

import { authGuard } from './guards/auth.guard'
import { GameComponent } from './pages/game/game.component'
import { HomeComponent } from './pages/home/home.component'
import { LoginComponent } from './pages/login/login.component'
import { RegisterComponent } from './pages/register/register.component'

export const routes: Routes = [
  {
    path: 'login',
    component: LoginComponent,
    canActivate: [authGuard({ reverse: true, redirectRoute: '/' })],
  },
  {
    path: 'register',
    component: RegisterComponent,
    canActivate: [authGuard({ reverse: true, redirectRoute: '/' })],
  },
  {
    path: 'games/:uuid',
    component: GameComponent,
    canActivate: [authGuard()],
  },
  {
    path: '**',
    component: HomeComponent,
    canActivate: [authGuard()],
  },
]

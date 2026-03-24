import { Injectable } from '@angular/core';
import { AuthService } from '../auth/auth.service';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client/dist/sockjs';
import { BehaviorSubject } from 'rxjs';
import { ConfigurationService } from '../configuration/configuration.service';

@Injectable({
    providedIn: 'root'
})
export class PlanWebSocketService {
  private stompClient$: BehaviorSubject<Client | null> = new BehaviorSubject<Client | null>(null);

  constructor(private authService: AuthService, private configurationService: ConfigurationService) {}


  public connect() {
    const serverUrl = this.configurationService.server.replace(/\/api\/?$/, '');
    const socket = new SockJS(`${serverUrl}/ws/plans`);

    const stompClient = new Client({
        webSocketFactory: () => socket,
        connectHeaders: {
			Authorization: `Bearer ${this.authService.currentAuthenticationToken()}`,
			'x-tenant': 'default'
        },
        onConnect: (frame) => {
            console.log("Connected:", frame);
            this.stompClient$.next(stompClient);
        },
        onDisconnect: () => {
            console.log("Disconnected");
            this.stompClient$.next(null);
        },
        debug: console.log
        });

    stompClient.activate();
  }

  public closeConnection() {
    const client = this.stompClient$.value;
    if (client && client.connected) {
      client.deactivate();
      console.log("Stop deactivated");
      this.stompClient$.next(null);
    }
  }

  public getClient$() {
    return this.stompClient$.asObservable();
  }
}
